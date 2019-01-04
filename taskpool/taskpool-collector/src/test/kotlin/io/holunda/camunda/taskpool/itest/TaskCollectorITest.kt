package io.holunda.camunda.taskpool.itest

import io.holunda.camunda.taskpool.api.task.*
import io.holunda.camunda.taskpool.api.task.CamundaTaskEvent.Companion.CREATE
import io.holunda.camunda.taskpool.sender.AxonCommandGatewayWrapper
import org.camunda.bpm.engine.RepositoryService
import org.camunda.bpm.engine.RuntimeService
import org.camunda.bpm.engine.TaskService
import org.camunda.bpm.engine.delegate.DelegateTask
import org.camunda.bpm.engine.delegate.TaskListener
import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareAssertions.assertThat
import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.*
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
import org.springframework.stereotype.Component
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
  lateinit var commandGateway: AxonCommandGatewayWrapper

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

    reset(commandGateway)
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

    verify(commandGateway).sendToGateway(listOf(deleteCommand))
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

    reset(commandGateway)
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

    verify(commandGateway).sendToGateway(listOf(completeCommand))
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

    reset(commandGateway)
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

    verify(commandGateway).sendToGateway(listOf(completeCommand))
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

    reset(commandGateway)
    val now = Date.from(now())
    val updateCommand = UpdateAttributeTaskCommand(
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
      description = null,
      taskDefinitionKey = taskDefinitionKey,
      dueDate = now,
      assignee = null,
      owner = null,
      priority = 50
    )

    // set due date to now
    taskService.saveTask(task().apply { dueDate = now })

    verify(commandGateway).sendToGateway(listOf(updateCommand))
  }

  /**
   * The process is started and runs into a user task.
   * The create command is send after the TX commit.
   */
  @Test
  fun `should send task create of async process`() {

    val businessKey = "BK1"
    val processId = "processId"
    val taskDefinitionKey = "userTask"

    // deploy
    repositoryService
      .createDeployment()
      .addModelInstance("process.bpmn",
        createUserTaskProcess(processId,
          taskDefinitionKey,
          taskListeners = listOf(
            Pair("create", "#{addCandidateUserPiggy}"),
            Pair("create", "#{addCandidateGroupMuppetShow}")),
          additionalUserTask = false,
          asyncOnStart = true))
      .deploy()

    // start
    val instance = runtimeService
      .startProcessInstanceByKey(
        processId,
        businessKey,
        Variables.putValue("key", "value")
      )

    // continue
    assertThat(instance).isNotNull
    assertThat(instance).isStarted
    execute(job())

    // user task
    assertThat(instance).isWaitingAt(taskDefinitionKey)

    val createCommand = CreateTaskCommand(
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
      candidateUsers = listOf("piggy"),
      candidateGroups = listOf("muppetshow"),
      enriched = true,
      eventName = CREATE,
      createTime = task().createTime,
      businessKey = "BK1",
      payload = Variables.putValue("key", Variables.stringValue("value"))
    )
    val updateCommand = UpdateAttributeTaskCommand(
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
      description = null,
      taskDefinitionKey = taskDefinitionKey,
      priority = 50,
      assignee = null,
      owner = null
    )
    val addCandidateUserCommand = AddCandidateUserCommand(
      id = task().id,
      userId = "piggy"
    )
    val addCandidateGroupCommand = AddCandidateGroupCommand(
      id = task().id,
      groupId = "muppetshow"
    )

    verify(commandGateway).sendToGateway(listOf(createCommand, addCandidateUserCommand, addCandidateGroupCommand, updateCommand))
  }

  /**
   * The process is started and wait in a user task.
   * The assign command is send after the TX commit.
   */
  @Test
  fun `should send task assign`() {

    val businessKey = "BK1"
    val processId = "processId"
    val taskDefinitionKey = "userTask"

    // deploy
    repositoryService
      .createDeployment()
      .addModelInstance("process.bpmn",
        createUserTaskProcess(processId,
          taskDefinitionKey,
          additionalUserTask = false))
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

    reset(commandGateway)
    val assignCommand = AssignTaskCommand(
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
      businessKey = "BK1",
      assignee = "kermit",
      createTime = task().createTime
    )
    val addCandidateUserCommand = AddCandidateUserCommand(
      id = task().id,
      userId = "kermit"
    )
    val updateAttributesTaskCommand = UpdateAttributeTaskCommand(
      id = task().id,
      sourceReference = ProcessReference(
        instanceId = instance.id,
        executionId = task().executionId,
        definitionId = task().processDefinitionId,
        name = "My Process",
        definitionKey = processId,
        applicationName = "collector-test"
      ),
      taskDefinitionKey = taskDefinitionKey,
      name = task().name,
      description = null,
      assignee = "kermit",
      owner = null,
      priority = 50,
      dueDate = null,
      followUpDate = null
    )

    // set due date to now
    taskService.setAssignee(task().id, "kermit")

    verify(commandGateway).sendToGateway(listOf(assignCommand, addCandidateUserCommand, updateAttributesTaskCommand))
  }

  /**
   * Creates a process model instance with start -> []user-task -> (optional: another-user-task) -> end
   */
  fun createUserTaskProcess(processId: String, taskDefinitionKey: String, additionalUserTask: Boolean = false, asyncOnStart: Boolean = false,
                            candidateGroups: String = "", candidateUsers: String = "", taskListeners: List<Pair<String, String>> = listOf()) =
    Bpmn
      .createExecutableProcess(processId)
      .startEvent("start").camundaAsyncAfter(asyncOnStart)
      .userTask(taskDefinitionKey).camundaCandidateGroups(candidateGroups).camundaCandidateUsers(candidateUsers)
      .apply {
        taskListeners.forEach {
          this.camundaTaskListenerDelegateExpression(it.first, it.second)
        }
      }
      .apply {
        if (additionalUserTask) {
          this.userTask("another-user-task")
        }
      }
      .endEvent("end")
      .done().apply {
        getModelElementById<ModelElementInstance>(processId).setAttributeValue("name", "My Process")
      }

}

@Component
class AddCandidateUserPiggy : TaskListener {
  override fun notify(delegateTask: DelegateTask?) {
    delegateTask!!.addCandidateUser("piggy")
  }
}

@Component
class AddCandidateGroupMuppetShow : TaskListener {
  override fun notify(delegateTask: DelegateTask?) {
    delegateTask!!.addCandidateGroup("muppetshow")
  }
}
