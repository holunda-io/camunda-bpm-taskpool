package io.holunda.polyflow.taskpool.itest.tx

import io.holunda.polyflow.taskpool.EnableCamundaTaskpoolCollector
import io.holunda.polyflow.taskpool.collector.process.definition.ProcessDefinitionService
import org.assertj.core.api.Assertions.assertThat
import org.axonframework.commandhandling.gateway.CommandGateway
import org.camunda.bpm.engine.FormService
import org.camunda.bpm.engine.ProcessEngine
import org.camunda.bpm.engine.RepositoryService
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl
import org.camunda.bpm.model.bpmn.Bpmn
import org.camunda.bpm.model.xml.instance.ModelElementInstance
import org.camunda.bpm.spring.boot.starter.annotation.EnableProcessApplication
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional

/**
 * This ITests simulates work of Camunda process definition collector.
 */
@SpringBootTest(classes = [ProcessDefinitionServiceITest.CollectorTestApplication::class], webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ActiveProfiles("collector-tx-itest")
@DirtiesContext
@Transactional
class ProcessDefinitionServiceITest {

  companion object {
    const val NS_CAMUNDA = "http://camunda.org/schema/1.0/bpmn"
  }

  @Autowired
  private lateinit var processDefinitionService: ProcessDefinitionService

  @Autowired
  private lateinit var repositoryService: RepositoryService

  @Autowired
  private lateinit var processEngine: ProcessEngine

  @Autowired
  private lateinit var formService: FormService

  @Test
  fun `should not run outside of command context`() {
    val exception = assertThrows<Exception> {
      processDefinitionService.getProcessDefinitions(formService, repositoryService)
    }
    assertThat(exception.message).isEqualTo("This method must be executed inside a Camunda command context.")
  }

  @Test
  fun `should deliver process starter`() {

    val processId = "my-id"
    val startEventId = "start"
    val modelInstance = Bpmn
      .createExecutableProcess(processId)
      .camundaHistoryTimeToLive(1)
      .startEvent(startEventId)
      .endEvent("end")
      .done().apply {
        getModelElementById<ModelElementInstance>(processId).setAttributeValue("name", "My Process")
        getModelElementById<ModelElementInstance>(processId).setAttributeValueNs(NS_CAMUNDA, "candidateStarterGroups", "muppetshow")
        getModelElementById<ModelElementInstance>(startEventId).setAttributeValueNs(NS_CAMUNDA, "formKey", "start-approval")
      }

    repositoryService
      .createDeployment()
      .addModelInstance("process-with-start-form.bpmn", modelInstance)
      .deploy()


    val definitions = processDefinitionService.getProcessDefinitions(processEngine.processEngineConfiguration as ProcessEngineConfigurationImpl)

    assertThat(definitions).isNotEmpty
    assertThat(definitions[0].processName).isEqualTo("My Process")
    assertThat(definitions[0].processDefinitionKey).isEqualTo("my-id")
    assertThat(definitions[0].processDefinitionVersion).isEqualTo(1)
    assertThat(definitions[0].formKey).isEqualTo("start-approval")
    assertThat(definitions[0].candidateStarterGroups).containsExactlyElementsOf(listOf("muppetshow"))
  }

  @Test
  fun `should not deliver process starter if no start form is available`() {

    val processId = "my-id"
    val startEventId = "start"
    val modelInstance = Bpmn
      .createExecutableProcess(processId)
      .camundaHistoryTimeToLive(1)
      .startEvent(startEventId)
      .endEvent("end")
      .done().apply {
        getModelElementById<ModelElementInstance>(processId).setAttributeValue("name", "My Process")
        getModelElementById<ModelElementInstance>(processId).setAttributeValueNs(NS_CAMUNDA, "candidateStarterGroups", "muppetshow")
      }

    repositoryService
      .createDeployment()
      .addModelInstance("process-without-start-form.bpmn", modelInstance)
      .deploy()


    val definitions = processDefinitionService.getProcessDefinitions(processEngine.processEngineConfiguration as ProcessEngineConfigurationImpl)

    assertThat(definitions).isNotEmpty
    assertThat(definitions[0].processName).isEqualTo("My Process")
    assertThat(definitions[0].processDefinitionKey).isEqualTo("my-id")
    assertThat(definitions[0].processDefinitionVersion).isEqualTo(1)
    assertThat(definitions[0].formKey).isNull()
    assertThat(definitions[0].candidateStarterGroups).containsExactlyElementsOf(listOf("muppetshow"))
  }

  @Test
  fun `should not deliver process starter if only two message start events are available`() {
    repositoryService
      .createDeployment()
      .addClasspathResource("itest/message_start_event.bpmn")
      .deploy()

    val definitions = processDefinitionService.getProcessDefinitions(processEngine.processEngineConfiguration as ProcessEngineConfigurationImpl)

    assertThat(definitions).isNotEmpty
    assertThat(definitions[0].processName).isEqualTo("My Process")
    assertThat(definitions[0].processDefinitionKey).isEqualTo("my-id")
    assertThat(definitions[0].processDefinitionVersion).isEqualTo(1)
    assertThat(definitions[0].formKey).isNull()
  }

  /**
   * Internal test application.
   */
  @SpringBootApplication
  @EnableProcessApplication
  @EnableCamundaTaskpoolCollector
  class CollectorTestApplication {
    /**
     * Gateway.
     */
    @Bean
    @Primary
    fun testAxonCommandGateway(): CommandGateway = org.mockito.kotlin.mock()

  }
}
