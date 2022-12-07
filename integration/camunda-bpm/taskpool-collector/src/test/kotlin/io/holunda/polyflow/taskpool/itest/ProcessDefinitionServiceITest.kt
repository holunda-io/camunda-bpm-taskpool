package io.holunda.polyflow.taskpool.itest

import io.holunda.polyflow.taskpool.collector.process.definition.ProcessDefinitionService
import org.assertj.core.api.Assertions.assertThat
import org.camunda.bpm.engine.FormService
import org.camunda.bpm.engine.ProcessEngine
import org.camunda.bpm.engine.RepositoryService
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl
import org.camunda.bpm.model.bpmn.Bpmn
import org.camunda.bpm.model.xml.instance.ModelElementInstance
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension

/**
 * This ITests simulates work of Camunda process definition collector.
 */
@SpringBootTest(classes = [CollectorTestApplication::class], webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ActiveProfiles("collector-itest")
@DirtiesContext
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

}
