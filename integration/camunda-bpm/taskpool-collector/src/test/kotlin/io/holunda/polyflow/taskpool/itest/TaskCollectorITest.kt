package io.holunda.polyflow.taskpool.itest

import com.fasterxml.jackson.module.kotlin.convertValue
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.holunda.camunda.taskpool.api.business.newCorrelations
import io.holunda.camunda.taskpool.api.task.*
import io.holunda.camunda.taskpool.api.task.CamundaTaskEventType.Companion.CREATE
import io.holunda.polyflow.taskpool.itest.TaskCollectorITest.Companion.NOW
import io.holunda.polyflow.taskpool.sender.gateway.CommandListGateway
import org.assertj.core.api.Assertions
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
import org.camunda.bpm.engine.variable.VariableMap
import org.camunda.bpm.engine.variable.Variables
import org.camunda.bpm.engine.variable.Variables.createVariables
import org.camunda.bpm.model.bpmn.Bpmn
import org.camunda.bpm.model.bpmn.BpmnModelInstance
import org.camunda.bpm.model.xml.instance.ModelElementInstance
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito.*
import org.mockito.kotlin.verifyNoMoreInteractions
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.stereotype.Component
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.time.Instant.now
import java.util.*
import java.util.concurrent.TimeUnit


/**
 * This ITests simulates work of Camunda collector including variable enrichment.
 */
@ExtendWith(SpringExtension::class)
@SpringBootTest(classes = [CollectorTestApplication::class], webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ActiveProfiles("collector-itest")
@DirtiesContext
class TaskCollectorITest {

  private val businessKey = "BK1"
  private val processId = "processId"
  private val taskDefinitionKey = "userTask"
  private val defaultVariables = createVariables().apply { put("key", "value") }

  companion object {
    val NOW = Date.from(now())
  }

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

    val reason = "I-test"
    // deploy
    deployProcess(createUserTaskProcess(processId, taskDefinitionKey))

    // start
    val instance = startProcessInstance(processId, businessKey)
    assertThat(instance).isWaitingAt(taskDefinitionKey)
    verify(commandListGateway).sendToGateway(listOf(createTaskCommand()))

    // delete
    val deleteCommand = DeleteTaskCommand(
      id = task().id,
      deleteReason = reason
    )
    runtimeService.deleteProcessInstance(instance.processInstanceId, reason, true) // set to skip listeners, still should work fine

    verify(commandListGateway).sendToGateway(listOf(deleteCommand))
    verifyNoMoreInteractions(commandListGateway)

  }

  /**
   * The process is started and wait in a user task. If this gets completed,
   * the process is finished (no wait states between user task and process end).
   */
  @Test
  fun `completes on process finish`() {

    // deploy
    deployProcess(createUserTaskProcess(processId, taskDefinitionKey))

    // start
    val instance = runtimeService.startProcessInstanceByKey(processId, businessKey, defaultVariables)
    assertThat(instance).isWaitingAt(taskDefinitionKey)
    verify(commandListGateway).sendToGateway(listOf(createTaskCommand()))


    // complete
    val completeCommand = CompleteTaskCommand(
      id = task().id
    )
    taskService.complete(task().id, Variables.putValue("input", "from user"))
    verify(commandListGateway).sendToGateway(listOf(completeCommand))

    verifyNoMoreInteractions(commandListGateway)
  }

  /**
   * The process is started and wait in a user task. If this gets completed,
   * the process runs to the next user task.
   */
  @Test
  fun `completes with variables and creates new task`() {

    // deploy
    deployProcess(createUserTaskProcess(processId, taskDefinitionKey, true, otherTaskDefinitionKey = "other-task"))

    // start
    val instance = startProcessInstance(processId, businessKey)
    assertThat(instance).isWaitingAt(taskDefinitionKey)
    verify(commandListGateway).sendToGateway(listOf(createTaskCommand()))

    // complete
    val completeCommand = CompleteTaskCommand(
      id = task().id
    )
    taskService.complete(task().id, Variables.putValue("input", "from user"))
    verify(commandListGateway).sendToGateway(listOf(completeCommand))

    // separate task id => separate command list
    assertThat(instance).isWaitingAt("other-task")
    val otherTaskCreateCommand = createTaskCommand(variables = defaultVariables.apply {
      put("input", "from user")
    })
    verify(commandListGateway).sendToGateway(listOf(otherTaskCreateCommand))

    verifyNoMoreInteractions(commandListGateway)
  }

  /**
   * The process is started and wait in a user task. If this gets claimed and completed,
   * the process runs to the next user task.
   */
  @Test
  fun `claim and complete in one TX`() {

    // deploy
    deployProcess(createUserTaskProcess(processId, taskDefinitionKey))

    // start
    val instance = startProcessInstance(processId, businessKey)
    assertThat(instance).isWaitingAt(taskDefinitionKey)
    verify(commandListGateway).sendToGateway(listOf(createTaskCommand()))


    val completeCommand = CompleteTaskCommand(
      id = task().id,
      assignee = "BudSpencer"
    )
    val doInOneTransactionCommand = Command {
      // claim
      taskService.claim(completeCommand.id, "BudSpencer")
      // complete
      taskService.complete(completeCommand.id, Variables.putValue("input", "from user"))
    }

    commandExecutor.execute(doInOneTransactionCommand)
    verify(commandListGateway).sendToGateway(listOf(completeCommand))
    verifyNoMoreInteractions(commandListGateway)
  }


  /**
   * The process is started and wait in a user task.
   * The update command is send after the TX commit.
   */
  @Test
  fun `updates attributes via API`() {

    // deploy
    deployProcess(createUserTaskProcess(processId, taskDefinitionKey))

    // start
    val instance = startProcessInstance(processId, businessKey)
    assertThat(instance).isWaitingAt(taskDefinitionKey)
    verify(commandListGateway).sendToGateway(listOf(createTaskCommand()))

    // set due date to now
    val now = Date.from(now())
    taskService.saveTask(task().apply {
      name = "new name"
      description = "new description"
      dueDate = now
    })

    val updateCommand = updateTaskCommand()
    verify(commandListGateway).sendToGateway(listOf(updateCommand))

    verifyNoMoreInteractions(commandListGateway)
  }

  /**
   * The process is started and runs into a user task.
   * The create command is send after the TX commit.
   */
  @Test
  fun `should send task create of async process`() {

    // deploy
    deployProcess(
      createUserTaskProcess(
        processId,
        taskDefinitionKey,
        taskListeners = listOf(
          Pair("create", "#{addCandidateUserPiggy}"),
          Pair("create", "#{addCandidateGroupMuppetShow}")
        ),
        additionalUserTask = false,
        asyncOnStart = true
      )
    )

    // start
    val instance = startProcessInstance(
      processId,
      businessKey
    )

    // wait for async continuation: we must not trigger the execution of the job explicitly but instead await its execution
    await().untilAsserted { assertThat(job(instance)).isNull() }

    // user task
    assertThat(instance).isWaitingAt(taskDefinitionKey)

    val createCommand = createTaskCommand(
      candidateUsers = setOf("piggy"),
      candidateGroups = setOf("muppetshow"),
    )
    // we need to take into account that dispatching the accumulated commands is done asynchronously, and therefore we might have to wait a little bit
    waitAtMost(3, TimeUnit.SECONDS).untilAsserted { verify(commandListGateway).sendToGateway(listOf(createCommand)) }
    verifyNoMoreInteractions(commandListGateway)
  }

  /**
   * Test case for the issue described in [io.holunda.polyflow.taskpool.collector.task.enricher.ProcessVariablesTaskCommandEnricher]
   * where variable changes were flushed in the inner process engine context, causing an OptimisticLockingException.
   */
  @Test
  fun `not flushes changes in separate context`() {
    // deploy
    deployProcess(
      createUserTaskProcess(
        processId,
        taskDefinitionKey,
        taskListeners = listOf(
          Pair("create", "#{addCandidateUserPiggy}"),
          Pair("create", "#{addCandidateGroupMuppetShow}")
        ),
        additionalUserTask = false,
        asyncOnStart = true
      )
    )

    // start
    val set = linkedSetOf("3", "2", "1")
    // When Jackson reads the set as a Set (not a LinkedSet), the order changes
    Assertions.assertThat(set.toList()).isNotEqualTo(jacksonObjectMapper().convertValue<Set<String>>(set).toList())
    val instance = startProcessInstance(
      processId, businessKey,
      Variables
        .putValue("key", "value")
        .putValue("object", MyStructureWithSet("name", "key", 1, set))
    )

    // wait for async continuation: we must not trigger the execution of the job explicitly but instead await its execution
    await().untilAsserted { assertThat(job(instance)).isNull() }

    // user task
    assertThat(instance).isWaitingAt(taskDefinitionKey)

    val createCommand = createTaskCommand(
      candidateUsers = setOf("piggy"), candidateGroups = setOf("muppetshow"), variables = Variables
        .putValue("key", Variables.stringValue("value"))
        // Jackson changes the order in the set, so we need to get the iteration order that the elements would have in a normal HashSet
        .putValue(
          "object",
          mapOf(
            MyStructureWithSet::name.name to "name",
            MyStructureWithSet::key.name to "key",
            MyStructureWithSet::value.name to 1,
            MyStructureWithSet::set.name to HashSet(set).toList()
          )
        )
    )

    // we need to take into account that dispatching the accumulated commands is done asynchronously, and therefore we might have to wait a little bit
    waitAtMost(3, TimeUnit.SECONDS).untilAsserted {
      verify(commandListGateway).sendToGateway(listOf(createCommand))
    }

    verifyNoMoreInteractions(commandListGateway)
  }

  /**
   * The process is started and wait in a user task.
   * The assign command is send after the TX commit.
   */
  @Test
  fun `(re) assigns user`() {

    // deploy
    deployProcess(
      createUserTaskProcess(
        processId, taskDefinitionKey,
        candidateUsers = "piggy, kermit",
        taskListeners = listOf(
          "create" to "#{setAssigneePiggy}" // will add a listener setting assignee to piggy on task creation.
        )
      )
    )

    // start
    val instance = startProcessInstance(processId, businessKey)
    assertThat(instance).isWaitingAt(taskDefinitionKey)
    verify(commandListGateway).sendToGateway(listOf(createTaskCommand(candidateUsers = setOf("piggy", "kermit"))))

    // assign
    val assignTaskCommand = AssignTaskCommand(
      id = task().id,
      assignee = "kermit"
    )

    taskService.setAssignee(task().id, "kermit")
    verify(commandListGateway).sendToGateway(listOf(assignTaskCommand))

    verifyNoMoreInteractions(commandListGateway)
  }

  @Test
  fun `update by listener is not loosing form key`() {
    // deploy
    deployProcess(
      createUserTaskProcess(
        processId, taskDefinitionKey,
        taskListeners = listOf(
          "create" to "#{changeTaskAttributes}" // will change direct task attributes
        )
      )
    )

    // start
    val instance = startProcessInstance(processId, businessKey)
    assertThat(instance).isWaitingAt(taskDefinitionKey)
    verify(commandListGateway).sendToGateway(listOf(
      createTaskCommand()
      .copy(
        name = "new name",
        description = "new description",
        priority = 99,
        dueDate = NOW,
        followUpDate = NOW,
//        name = "User Task",
//        description = null,
//        priority = 66,
//        dueDate = null,
//        followUpDate = null,

      )
    ))

  }

  /**
   * The process is started and wait in a user task.
   * The candidate group change commands is sent after the TX commit.
   */
  @Test
  fun `changes candidate groups via API`() {

    val muppets = "muppets"
    val lords = "lords"
    val peasants = "peasants"
    val avengers = "avengers"
    val dwarfs = "dwarfs"

    // deploy
    deployProcess(
      createUserTaskProcess(
        processId,
        taskDefinitionKey,
        additionalUserTask = false,
        candidateGroups = "$muppets,$lords,$peasants"
      )
    )

    // start
    val instance = startProcessInstance(processId, businessKey)
    assertThat(instance).isWaitingAt(taskDefinitionKey)
    assertThat(task()).hasCandidateGroup(muppets)
    assertThat(task()).hasCandidateGroup(peasants)
    assertThat(task()).hasCandidateGroup(lords)
    verify(commandListGateway).sendToGateway(listOf(createTaskCommand(
      candidateGroups = setOf(muppets, peasants, lords)
    )))

    val doInOneTransactionCommand = Command {
      taskService.deleteCandidateGroup(task().id, muppets)
      taskService.deleteCandidateGroup(task().id, peasants)
      taskService.deleteCandidateGroup(task().id, lords)
      taskService.addCandidateGroup(task().id, avengers)
      taskService.addCandidateGroup(task().id, dwarfs)
    }
    commandExecutor.execute(doInOneTransactionCommand)

    val deleteGroups = DeleteCandidateGroupsCommand(task().id, candidateGroups = setOf(muppets, peasants, lords))
    val addGroups = AddCandidateGroupsCommand(task().id, candidateGroups = setOf(avengers, dwarfs))
    verify(commandListGateway).sendToGateway(listOf(deleteGroups, addGroups))
  }

  /**
   * The process is started and wait in a user task.
   * The candidate group change commands is sent after the TX commit.
   */
  @Test
  fun `changes candidate users via API`() {

    val kermit = "kermit"
    val piggy = "piggy"
    val superman = "superman"
    val batman = "batman"
    val hulk = "hulk"

    // deploy
    deployProcess(
      createUserTaskProcess(
        processId,
        taskDefinitionKey,
        candidateUsers = "$kermit,$piggy"
      )
    )

    // start
    val instance = startProcessInstance(processId, businessKey)
    assertThat(instance).isWaitingAt(taskDefinitionKey)
    assertThat(task()).hasCandidateUser(kermit)
    assertThat(task()).hasCandidateUser(piggy)
    verify(commandListGateway).sendToGateway(listOf(createTaskCommand(
      candidateUsers = setOf(kermit, piggy)
    )))

    val doInOneTransactionCommand = Command {
      taskService.deleteCandidateUser(task().id, kermit)
      taskService.deleteCandidateUser(task().id, piggy)
      taskService.addCandidateUser(task().id, superman)
      taskService.addCandidateUser(task().id, batman)
      taskService.addCandidateUser(task().id, hulk)

    }
    commandExecutor.execute(doInOneTransactionCommand)

    val deleteUsers = DeleteCandidateUsersCommand(task().id, candidateUsers = setOf(kermit, piggy))
    val addUsers = AddCandidateUsersCommand(task().id, candidateUsers = setOf(superman, batman, hulk))
    verify(commandListGateway).sendToGateway(listOf(deleteUsers, addUsers))
  }


  /*
   * Deploys the process.
   */
  private fun deployProcess(modelInstance: BpmnModelInstance) {
    repositoryService
      .createDeployment()
      .addModelInstance("process.bpmn", modelInstance)
      .deploy()
  }

  /*
   * Starts the process.
   */
  private fun startProcessInstance(
    processId: String,
    businessKey: String,
    variables: VariableMap = defaultVariables
  ) = runtimeService
    .startProcessInstanceByKey(
      processId,
      businessKey,
      variables
    ).also { instance ->
      assertThat(instance).isNotNull
      assertThat(instance).isStarted
    }

  /**
   * Creates a process model instance with start -> user-task -> (optional: another-user-task) -> end
   */
  private fun createUserTaskProcess(
    processId: String,
    taskDefinitionKey: String,
    additionalUserTask: Boolean = false,
    asyncOnStart: Boolean = false,
    candidateGroups: String = "",
    candidateUsers: String = "",
    formKey: String = "form-key",
    taskListeners: List<Pair<String, String>> = listOf(),
    otherTaskDefinitionKey: String = "another-user-task"
  ) = Bpmn
    .createExecutableProcess(processId)
    // start event
    .startEvent("start").camundaAsyncAfter(asyncOnStart)
    // user task
    .userTask(taskDefinitionKey)
    .camundaCandidateGroups(candidateGroups)
    .camundaCandidateUsers(candidateUsers)
    .camundaFormKey(formKey)
    .camundaPriority("66")
    .apply {
      taskListeners.forEach {
        this.camundaTaskListenerDelegateExpression(it.first, it.second)
      }
      this.element.name = "User Task"
    }
    // optional second user task
    .apply {
      if (additionalUserTask) {
        this.userTask(otherTaskDefinitionKey)
      }
    }
    // end event
    .endEvent("end")
    .done().apply {
      getModelElementById<ModelElementInstance>(processId).setAttributeValue("name", "My Process")
    }


  /*
   * Create task command from current task.
   */
  private fun createTaskCommand(
    candidateGroups: Set<String> = setOf(),
    candidateUsers: Set<String> = setOf(),
    variables: VariableMap = defaultVariables,
    processBusinessKey: String = this.businessKey
  ) = task(taskQuery().initializeFormKeys()).let { task ->
    CreateTaskCommand(
      id = task.id,
      sourceReference = ProcessReference(
        instanceId = task.processInstanceId,
        executionId = task.executionId,
        definitionId = task.processDefinitionId,
        name = "My Process",
        definitionKey = processId,
        applicationName = "collector-test"
      ),
      name = task.name,
      description = task.description,
      taskDefinitionKey = task.taskDefinitionKey,
      candidateUsers = candidateUsers,
      candidateGroups = candidateGroups,
      assignee = task.assignee,
      enriched = true,
      eventName = CREATE,
      createTime = task.createTime,
      businessKey = processBusinessKey,
      priority = task.priority, // default by camunda if not set in explicit
      payload = variables,
      formKey = task.formKey
    )
  }

  /*
   * Creates update command from current task.
   */
  private fun updateTaskCommand(
    variables: VariableMap = defaultVariables,
    instanceBusinessKey: String = businessKey,
    correlations: VariableMap = newCorrelations()
  ) =
    task(taskQuery().initializeFormKeys()).let { task ->
      UpdateAttributeTaskCommand(
        id = task.id,
        name = task.name,
        description = task.description,
        dueDate = task.dueDate,
        owner = task.owner,
        priority = task.priority,
        taskDefinitionKey = task.taskDefinitionKey,
        sourceReference = ProcessReference(
          instanceId = task.processInstanceId,
          executionId = task.executionId,
          definitionId = task.processDefinitionId,
          name = "My Process",
          definitionKey = processId,
          applicationName = "collector-test"
        ),
        enriched = true,
        businessKey = instanceBusinessKey,
        payload = variables,
        correlations = correlations,
        formKey = task.formKey
      )
    }
}

@Component
internal class AddCandidateUserPiggy : TaskListener {
  override fun notify(delegateTask: DelegateTask) {
    delegateTask.addCandidateUser("piggy")
  }
}

@Component
internal class SetAssigneePiggy : TaskListener {
  override fun notify(delegateTask: DelegateTask) {
    delegateTask.assignee = "piggy"
  }
}


@Component
class AddCandidateGroupMuppetShow : TaskListener {
  override fun notify(delegateTask: DelegateTask) {
    delegateTask.addCandidateGroup("muppetshow")
  }
}

/**
 * Typical use case for a start listener changing attributes
 */
@Component
class ChangeTaskAttributes : TaskListener {
  override fun notify(delegateTask: DelegateTask) {
    delegateTask.name = "new name"
    delegateTask.description = "new description"
    delegateTask.priority = 99
    delegateTask.dueDate = NOW
    delegateTask.followUpDate = NOW
  }
}



data class MyStructure(val name: String, val key: String, val value: Int)
data class MyStructureWithSet(val name: String, val key: String, val value: Int, val set: Set<String> = setOf())
