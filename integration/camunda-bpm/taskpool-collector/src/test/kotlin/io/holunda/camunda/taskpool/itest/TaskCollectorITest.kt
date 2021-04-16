package io.holunda.camunda.taskpool.itest

import io.holunda.camunda.taskpool.api.task.*
import io.holunda.camunda.taskpool.api.task.CamundaTaskEventType.Companion.CREATE
import io.holunda.camunda.taskpool.sender.gateway.CommandListGateway
import org.awaitility.Awaitility.await
import org.awaitility.Awaitility.waitAtMost
import org.camunda.bpm.engine.RepositoryService
import org.camunda.bpm.engine.RuntimeService
import org.camunda.bpm.engine.TaskService
import org.camunda.bpm.engine.delegate.DelegateTask
import org.camunda.bpm.engine.delegate.TaskListener
import org.camunda.bpm.engine.impl.interceptor.Command
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor
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
import java.io.Serializable
import java.time.Instant.now
import java.util.*
import java.util.concurrent.TimeUnit


/**
 * This ITests simulates work of Camunda collector including variable enrichment.
 */
@RunWith(SpringRunner::class)
@SpringBootTest(classes = [CollectorTestApplication::class], webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ActiveProfiles("collector-itest")
@DirtiesContext
class TaskCollectorITest {


  @Autowired
  lateinit var repositoryService: RepositoryService

  @Autowired
  lateinit var runtimeService: RuntimeService

  @Autowired
  lateinit var taskService: TaskService

  @Autowired
  lateinit var commandExecutor: CommandExecutor

  @MockBean
  lateinit var commandListGateway: CommandListGateway

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

    reset(commandListGateway)
    val deleteCommand = DeleteTaskCommand(
      id = task().id,
      deleteReason = reason
    )

    // delete
    runtimeService.deleteProcessInstance(instance.processInstanceId, reason, false)

    verify(commandListGateway).sendToGateway(listOf(deleteCommand))
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

    reset(commandListGateway)
    val completeCommand = CompleteTaskCommand(
      id = task().id
    )

    // complete
    taskService.complete(task().id, Variables.putValue("input", "from user"))

    verify(commandListGateway).sendToGateway(listOf(completeCommand))
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

    reset(commandListGateway)
    val completeCommand = CompleteTaskCommand(
      id = task().id
    )

    // complete
    taskService.complete(task().id, Variables.putValue("input", "from user"))

    verify(commandListGateway).sendToGateway(listOf(completeCommand))
  }

  /**
   * The process is started and wait in a user task. If this gets claimed and completed,
   * the process runs to the next user task.
   * The complete command is send after the TX commit and contains all process
   * variables (prior to complete and after the complete)
   */
  @Test
  fun `should do task claim and complete`() {

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

    reset(commandListGateway)
    val completeCommand = CompleteTaskCommand(
      id = task().id,
      assignee = "BudSpencer"
    )

    val doInOneTransactionCommand = Command {
      taskService.claim(task().id, "BudSpencer")
      // complete
      taskService.complete(task().id, Variables.putValue("input", "from user"))
    }

    commandExecutor.execute(doInOneTransactionCommand)

    verify(commandListGateway).sendToGateway(listOf(completeCommand))
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

    reset(commandListGateway)
    val now = Date.from(now())
    val updateCommand = UpdateAttributeTaskCommand(
      id = task().id,
      name = task().name,
      description = null,
      dueDate = now,
      owner = null,
      priority = 50,
      taskDefinitionKey = taskDefinitionKey,
      sourceReference = ProcessReference(
        instanceId = instance.id,
        executionId = task().executionId,
        definitionId = task().processDefinitionId,
        name = "My Process",
        definitionKey = processId,
        applicationName = "collector-test"
      ),
      enriched = true,
      payload = Variables.putValue("key", Variables.stringValue("value"))
    )

    // set due date to now
    taskService.saveTask(task().apply { dueDate = now })

    verify(commandListGateway).sendToGateway(listOf(updateCommand))
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
        Variables
          .putValue("key", "value")
          .putValue("object", MyStructure("name", "key", 1))
      )

    // wait for async continuation: we must not trigger the execution of the job explicitly but instead await its execution
    assertThat(instance).isNotNull
    assertThat(instance).isStarted
    await().untilAsserted { assertThat(job(instance)).isNull() }

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
      candidateUsers = setOf("piggy"),
      candidateGroups = setOf("muppetshow"),
      enriched = true,
      eventName = CREATE,
      createTime = task().createTime,
      businessKey = "BK1",
      priority = 50, // default by camunda if not set in explicit
      payload = Variables
        .putValue("key", Variables.stringValue("value"))
        .putValue("object", mapOf(MyStructure::name.name to "name", MyStructure::key.name to "key", MyStructure::value.name to 1))
    )

    // we need to take into account that dispatching the accumulated commands is done asynchronously and therefore we might have to wait a little bit
    waitAtMost(1, TimeUnit.SECONDS).untilAsserted { verify(commandListGateway).sendToGateway(listOf(createCommand)) }
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

    reset(commandListGateway)
    val assignTaskCommand = AssignTaskCommand(
      id = task().id,
      assignee = "kermit"
    )

    taskService.setAssignee(task().id, "kermit")

    verify(commandListGateway).sendToGateway(listOf(assignTaskCommand))
  }

  /**
   * Creates a process model instance with start -> user-task -> (optional: another-user-task) -> end
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
internal class AddCandidateUserPiggy : TaskListener {
  override fun notify(delegateTask: DelegateTask) {
    delegateTask.addCandidateUser("piggy")
  }
}

@Component
class AddCandidateGroupMuppetShow : TaskListener {
  override fun notify(delegateTask: DelegateTask) {
    delegateTask.addCandidateGroup("muppetshow")
  }
}

data class MyStructure(val name: String, val key: String, val value: Int) : Serializable
