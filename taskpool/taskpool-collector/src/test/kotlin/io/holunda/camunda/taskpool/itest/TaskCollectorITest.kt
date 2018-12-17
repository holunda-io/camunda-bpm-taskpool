package io.holunda.camunda.taskpool.itest

import io.holunda.camunda.taskpool.api.task.CamundaTaskEvent.Companion.ATTRIBUTES
import io.holunda.camunda.taskpool.api.task.CompleteTaskCommand
import io.holunda.camunda.taskpool.api.task.DeleteTaskCommand
import io.holunda.camunda.taskpool.api.task.InitialTaskCommand
import io.holunda.camunda.taskpool.api.task.ProcessReference
import io.holunda.camunda.taskpool.sender.CommandGatewayProxy
import org.camunda.bpm.engine.RepositoryService
import org.camunda.bpm.engine.RuntimeService
import org.camunda.bpm.engine.TaskService
import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareAssertions.assertThat
import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.task
import org.camunda.bpm.engine.variable.Variables
import org.camunda.bpm.model.bpmn.Bpmn
import org.camunda.bpm.model.xml.instance.ModelElementInstance
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.reset
import org.mockito.Mockito.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner
import java.time.Instant.now
import java.util.*


/**
 * This ITests simulates work of Camunda collector including variable enrichment.
 */
@RunWith(SpringRunner::class)
@SpringBootTest(classes = [TaskCollectorTestApplication::class], webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("collector-itest")
@DirtiesContext
class TaskCollectorITest {

  @MockBean
  lateinit var commandGatewayProxy: CommandGatewayProxy

  @Autowired
  lateinit var repositoryService: RepositoryService

  @Autowired
  lateinit var runtimeService: RuntimeService

  @Autowired
  lateinit var taskService: TaskService


  /**
   * The process is started and waits in the user task. If the instance is deleted,
   * and the listeners are notified, the delete command is sent out with process
   * variables contained prior the deletion.
   */
  @Test
  fun `should send task delete on process instance deletion`() {

    val businessKey = "BK1"
    val processId = "processId"
    val taskDefinitionKey = "userTask"
    val reason = "I-test"

    // deploy
    repositoryService
      .createDeployment()
      .addModelInstance("process.bpmn", createUserTaskProcess(processId, taskDefinitionKey))
      .deploy()

    // start
    val instance = runtimeService
      .startProcessInstanceByKey(
        processId,
        businessKey,
        Variables.putValue("key", "value")
      )
    assertThat(instance).isNotNull
    assertThat(instance).isStarted
    assertThat(instance).isWaitingAt(taskDefinitionKey)

    reset(commandGatewayProxy)
    val deleteCommand = DeleteTaskCommand(
      id = task().id,
      sourceReference = ProcessReference(
        instanceId = instance.id,
        executionId = task().executionId,
        definitionId = task().processDefinitionId,
        name = "My Process",
        definitionKey = processId,
        applicationName = "collector-test"
      ),
      createTime = task().createTime,
      name = task().name,
      businessKey = businessKey,
      taskDefinitionKey = taskDefinitionKey,
      enriched = false,
      deleteReason = reason
    )

    // delete
    runtimeService.deleteProcessInstance(instance.processInstanceId, reason, false)

    verify(commandGatewayProxy).send(deleteCommand)
  }

  /**
   * The process is started and wait in a user task. If this gets completed,
   * the process is finished (no wait states between user task and process end).
   * The complete command is send after the TX commit and contains all process
   * variables (prior to complete and after the complete)
   */
  @Test
  fun `should send task complete on process finish`() {

    val businessKey = "BK1"
    val processId = "processId"
    val taskDefinitionKey = "userTask"

    // deploy
    repositoryService
      .createDeployment()
      .addModelInstance("process.bpmn", createUserTaskProcess(processId, taskDefinitionKey))
      .deploy()

    // start
    val instance = runtimeService
      .startProcessInstanceByKey(
        processId,
        businessKey,
        Variables.putValue("key", "value")
      )
    assertThat(instance).isNotNull
    assertThat(instance).isStarted
    assertThat(instance).isWaitingAt(taskDefinitionKey)

    reset(commandGatewayProxy)
    val completeCommand = CompleteTaskCommand(
      id = task().id,
      sourceReference = ProcessReference(
        instanceId = instance.id,
        executionId = task().executionId,
        definitionId = task().processDefinitionId,
        name = "My Process",
        definitionKey = processId,
        applicationName = "collector-test"
      ),
      createTime = task().createTime,
      name = task().name,
      businessKey = businessKey,
      taskDefinitionKey = taskDefinitionKey,
      payload = Variables.putValue("key", "value").putValue("input", "from user"),
      enriched = true
    )

    // complete
    taskService.complete(task().id, Variables.putValue("input", "from user"))

    verify(commandGatewayProxy).send(completeCommand)
  }

  /**
   * The process is started and wait in a user task. If this gets completed,
   * the process runs to the next user task.
   * The complete command is send after the TX commit and contains all process
   * variables (prior to complete and after the complete)
   */
  @Test
  fun `should send task complete`() {

    val businessKey = "BK1"
    val processId = "processId"
    val taskDefinitionKey = "userTask"

    // deploy
    repositoryService
      .createDeployment()
      .addModelInstance("process.bpmn",
        createUserTaskProcess(processId,
          taskDefinitionKey,
          true))
      .deploy()

    // start
    val instance = runtimeService
      .startProcessInstanceByKey(
        processId,
        businessKey,
        Variables.putValue("key", "value")
      )
    assertThat(instance).isNotNull
    assertThat(instance).isStarted
    assertThat(instance).isWaitingAt(taskDefinitionKey)

    reset(commandGatewayProxy)
    val completeCommand = CompleteTaskCommand(
      id = task().id,
      sourceReference = ProcessReference(
        instanceId = instance.id,
        executionId = task().executionId,
        definitionId = task().processDefinitionId,
        name = "My Process",
        definitionKey = processId,
        applicationName = "collector-test"
      ),
      createTime = task().createTime,
      name = task().name,
      businessKey = businessKey,
      taskDefinitionKey = taskDefinitionKey,
      payload = Variables.putValue("key", "value").putValue("input", "from user"),
      enriched = true
    )

    // complete
    taskService.complete(task().id, Variables.putValue("input", "from user"))

    verify(commandGatewayProxy).send(completeCommand)
  }


  /**
   * The process is started and wait in a user task.
   * The update command is send after the TX commit.
   */
  @Test
  fun `should send task update`() {

    val businessKey = "BK1"
    val processId = "processId"
    val taskDefinitionKey = "userTask"

    // deploy
    repositoryService
      .createDeployment()
      .addModelInstance("process.bpmn",
        createUserTaskProcess(processId,
          taskDefinitionKey,
          false))
      .deploy()

    // start
    val instance = runtimeService
      .startProcessInstanceByKey(
        processId,
        businessKey,
        Variables.putValue("key", "value")
      )
    assertThat(instance).isNotNull
    assertThat(instance).isStarted
    assertThat(instance).isWaitingAt(taskDefinitionKey)

    reset(commandGatewayProxy)
    val now = Date.from(now())
    val updateCommand = InitialTaskCommand(
      id = task().id,
      sourceReference = ProcessReference(
        instanceId = instance.id,
        executionId = task().executionId,
        definitionId = task().processDefinitionId,
        name = "My Process",
        definitionKey = processId,
        applicationName = "collector-test"
      ),
      name = task().name,
      taskDefinitionKey = taskDefinitionKey,
      enriched = false,
      eventName = ATTRIBUTES,
      dueDate = now
    )

    // set due date to now
    taskService.saveTask(task().apply { dueDate = now })

    verify(commandGatewayProxy).send(updateCommand)
  }

  /**
   * Creates a process model instance with start -> []user-task -> (optional: another-user-task) -> end
   */
  fun createUserTaskProcess(processId: String, taskDefinitionKey: String, additionalUserTask: Boolean = false) =
    Bpmn
      .createExecutableProcess(processId)
      .startEvent("start")
      .userTask(taskDefinitionKey).apply {
        if (additionalUserTask) {
          this.userTask("another-user-task")
        }
      }
      .endEvent("end")
      .done().apply {
        getModelElementById<ModelElementInstance>(processId).setAttributeValue("name", "My Process")
      }

}
