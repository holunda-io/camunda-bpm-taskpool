package io.holunda.camunda.taskpool.itest

import io.holunda.camunda.taskpool.process.ProcessDefinitionService
import io.holunda.camunda.taskpool.sender.gateway.AxonCommandListGateway
import org.assertj.core.api.Assertions.assertThat
import org.camunda.bpm.engine.FormService
import org.camunda.bpm.engine.RepositoryService
import org.camunda.bpm.model.bpmn.Bpmn
import org.camunda.bpm.model.xml.instance.ModelElementInstance
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner


/**
 * This ITests simulates work of Camunda process definition collector.
 */
@RunWith(SpringRunner::class)
@SpringBootTest(classes = [TaskCollectorTestApplication::class], webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("collector-itest")
@DirtiesContext
class ProcessDefinitionServiceITest {

  companion object {
    const val NS_CAMUNDA = "http://camunda.org/schema/1.0/bpmn"
  }


  @MockBean
  lateinit var commandGateway: AxonCommandListGateway

  @Autowired
  private lateinit var processDefinitionService: ProcessDefinitionService

  @Autowired
  private lateinit var repositoryService: RepositoryService

  @Autowired
  private lateinit var formService: FormService

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

    val definitions = processDefinitionService.getProcessDefinitions(formService, repositoryService)

    assertThat(definitions).isNotEmpty
  }

}
