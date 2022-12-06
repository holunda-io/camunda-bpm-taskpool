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
@SpringBootTest(classes = [CollectorTestApplication::class], webEnvironment = SpringBootTest.WebEnvironment.MOCK, properties = ["polyflow.integration.sender.task.batchCommands=true"])
@ActiveProfiles("collector-itest")
@DirtiesContext
class TaskCollectorBatchITest {

  private val businessKey = "BK1"
  private val processId = "processId"
  private val taskDefinitionKey = "userTask"
  private val defaultVariables = createVariables().apply { put("key", "value") }

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
   * The process is started and wait in a user task.
   * The candidate group change commands is sent after the TX commit.
   */
  @Test
  fun `changes candidate groups via API as a batch`() {

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
    verify(commandListGateway).sendToGateway(
      listOf(
        createTaskCommand(
          candidateGroups = setOf(muppets, peasants, lords)
        )
      )
    )

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
    verify(commandListGateway).sendToGateway(listOf(BatchCommand(id = task().id, commands = listOf(deleteGroups, addGroups))))
  }

  /**
   * The process is started and wait in a user task.
   * The candidate group change commands is sent after the TX commit.
   */
  @Test
  fun `changes candidate groups and further command attributes via API as a batch`() {

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
    verify(commandListGateway).sendToGateway(
      listOf(
        createTaskCommand(
          candidateGroups = setOf(muppets, peasants, lords)
        )
      )
    )

    val doInOneTransactionCommand = Command {
      taskService.deleteCandidateGroup(task().id, muppets)
      taskService.deleteCandidateGroup(task().id, peasants)
      taskService.deleteCandidateGroup(task().id, lords)
      taskService.addCandidateGroup(task().id, avengers)
      taskService.addCandidateGroup(task().id, dwarfs)
      taskService.setPriority(task().id, 11)
      taskService.setAssignee(task().id, "kermit")
    }
    commandExecutor.execute(doInOneTransactionCommand)

    val deleteGroups = DeleteCandidateGroupsCommand(task().id, candidateGroups = setOf(muppets, peasants, lords))
    val addGroups = AddCandidateGroupsCommand(task().id, candidateGroups = setOf(avengers, dwarfs))
    val updateAttributes = updateTaskCommand().copy(
      priority = 11
    )
    val assign = AssignTaskCommand(task().id, assignee = "kermit")
    verify(commandListGateway).sendToGateway(listOf(BatchCommand(id = task().id, commands = listOf(assign, deleteGroups, addGroups, updateAttributes))))

  }

  /**
   * The process is started and wait in a user task.
   * The candidate group change commands is sent after the TX commit.
   */
  @Test
  fun `changes candidate users via API as a batch`() {

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
    verify(commandListGateway).sendToGateway(
      listOf(
        createTaskCommand(
          candidateUsers = setOf(kermit, piggy)
        )
      )
    )

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
    verify(commandListGateway).sendToGateway(listOf(BatchCommand(id = task().id, commands =listOf(deleteUsers, addUsers))))
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
        followUpDate = task.followUpDate,
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
        correlations = correlations
      )
    }
}
