package io.holunda.polyflow.taskpool.itest

import com.fasterxml.jackson.module.kotlin.convertValue
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.holunda.camunda.taskpool.api.task.*
import io.holunda.polyflow.taskpool.itest.TestDriver.Companion.DEFAULT_VARIABLES
import io.holunda.polyflow.taskpool.itest.TestDriver.Companion.createTaskCommand
import io.holunda.polyflow.taskpool.itest.TestDriver.Companion.createUserTaskProcess
import io.holunda.polyflow.taskpool.itest.TestDriver.Companion.updateTaskCommand
import io.holunda.polyflow.taskpool.sender.gateway.CommandListGateway
import org.assertj.core.api.Assertions
import org.awaitility.Awaitility.await
import org.awaitility.Awaitility.waitAtMost
import org.camunda.bpm.engine.RepositoryService
import org.camunda.bpm.engine.RuntimeService
import org.camunda.bpm.engine.TaskService
import org.camunda.bpm.engine.impl.interceptor.Command
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor
import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.*
import org.camunda.bpm.engine.variable.Variables
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.mockito.kotlin.verifyNoMoreInteractions
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import java.time.Instant.now
import java.util.*
import java.util.concurrent.TimeUnit


@SpringBootTest(classes = [CollectorTestApplication::class], webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ActiveProfiles("collector-itest")
@DirtiesContext
internal class TaskCollectorITest {

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

  private val driver: TestDriver by lazy {
    TestDriver(repositoryService, runtimeService)
  }

  @Test
  fun `creates task in process`() {

    // deploy
    driver.deployProcess(
      createUserTaskProcess(
        taskListeners = listOf(
          Pair("create", "#{addCandidateUserPiggy}"),
          Pair("create", "#{addCandidateGroupMuppetShow}"),
        ),
      )
    )

    // start
    val instance = driver.startProcessInstance()

    // wait for async continuation: we must not trigger the execution of the job explicitly but instead await its execution
    await().untilAsserted { assertThat(job(instance)).isNull() }

    // user task
    driver.assertProcessInstanceWaitsInUserTask(instance)

    val createCommand = createTaskCommand(
      candidateUsers = setOf("piggy"),
      candidateGroups = setOf("muppetshow"),
    )
    // we need to take into account that dispatching the accumulated commands is done asynchronously, and therefore we might have to wait a little bit
    waitAtMost(3, TimeUnit.SECONDS).untilAsserted { verify(commandListGateway).sendToGateway(listOf(createCommand)) }
    verifyNoMoreInteractions(commandListGateway)
  }

  @Test
  fun `creates task with values modified by task create listener`() {
    // deploy
    driver.deployProcess(
      createUserTaskProcess(
        taskListeners = listOf(
          "create" to "#{changeTaskAttributes}" // will change direct task attributes
        )
      )
    )

    // start
    val instance = driver.startProcessInstance()
    driver.assertProcessInstanceWaitsInUserTask(instance)
    verify(commandListGateway).sendToGateway(
      listOf(
        createTaskCommand()
          .copy(
            name = "new name",
            description = "new description",
            priority = 99,
            dueDate = TestDriver.NOW,
            followUpDate = TestDriver.NOW,
// Values from the original are, but these are modified by the listener.
//        name = "User Task",
//        description = null,
//        priority = 66,
//        dueDate = null,
//        followUpDate = null,
          )
      )
    )
  }


  @Test
  fun `creates task in process with async start`() {

    // deploy
    driver.deployProcess(
      createUserTaskProcess(
        taskListeners = listOf(
          Pair("create", "#{addCandidateUserPiggy}"),
          Pair("create", "#{addCandidateGroupMuppetShow}")
        ),
        asyncOnStart = true
      )
    )

    // start
    val instance = driver.startProcessInstance()

    // wait for async continuation: we must not trigger the execution of the job explicitly but instead await its execution
    await().untilAsserted { assertThat(job(instance)).isNull() }

    // user task
    driver.assertProcessInstanceWaitsInUserTask(instance)

    val createCommand = createTaskCommand(
      candidateUsers = setOf("piggy"),
      candidateGroups = setOf("muppetshow"),
    )
    // we need to take into account that dispatching the accumulated commands is done asynchronously, and therefore we might have to wait a little bit
    waitAtMost(3, TimeUnit.SECONDS).untilAsserted { verify(commandListGateway).sendToGateway(listOf(createCommand)) }
    verifyNoMoreInteractions(commandListGateway)
  }

  @Test
  fun `creates task in a process with async start and complex variables`() {
    // deploy
    driver.deployProcess(
      createUserTaskProcess(
        taskListeners = listOf(
          Pair("create", "#{addCandidateUserPiggy}"),
          Pair("create", "#{addCandidateGroupMuppetShow}")
        ),
        asyncOnStart = true
      )
    )

    // start
    val set = linkedSetOf("3", "2", "1")
    // When Jackson reads the set as a Set (not a LinkedSet), the order changes
    Assertions.assertThat(set.toList()).isNotEqualTo(jacksonObjectMapper().convertValue<Set<String>>(set).toList())
    val instance = driver.startProcessInstance(
      variables = Variables
        .putValue("key", "value")
        .putValue("object", MyStructureWithSet("name", "key", 1, set))
    )

    // wait for async continuation: we must not trigger the execution of the job explicitly but instead await its execution
    await().untilAsserted { assertThat(job(instance)).isNull() }

    // user task
    driver.assertProcessInstanceWaitsInUserTask(instance)

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
   * The process is started and wait in a user task. If this gets completed,
   * the process is finished (no wait states between user task and process end).
   */
  @Test
  fun `completes on process finish`() {

    // deploy
    driver.deployProcess(createUserTaskProcess())

    // start
    val instance = driver.startProcessInstance()
    driver.assertProcessInstanceWaitsInUserTask(instance)
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
    driver.deployProcess(createUserTaskProcess(additionalUserTask = true, otherTaskDefinitionKey = "other-task"))

    // start
    val instance = driver.startProcessInstance()
    driver.assertProcessInstanceWaitsInUserTask(instance)
    verify(commandListGateway).sendToGateway(listOf(createTaskCommand()))

    // complete
    val completeCommand = CompleteTaskCommand(
      id = task().id
    )
    taskService.complete(task().id, Variables.putValue("input", "from user"))
    verify(commandListGateway).sendToGateway(listOf(completeCommand))

    // separate task id => separate command list
    assertThat(instance).isWaitingAt("other-task")
    val otherTaskCreateCommand = createTaskCommand(variables = DEFAULT_VARIABLES.apply {
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
  fun `completes with claim in one TX`() {

    // deploy
    driver.deployProcess(createUserTaskProcess())

    // start
    val instance = driver.startProcessInstance()
    driver.assertProcessInstanceWaitsInUserTask(instance)
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
    driver.deployProcess(createUserTaskProcess())

    // start
    val instance = driver.startProcessInstance()
    driver.assertProcessInstanceWaitsInUserTask(instance)
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
   * The process is started and wait in a user task.
   * The update command is send after the TX commit.
   */
  @Test
  fun `updates attributes via API with update listener`() {

    // deploy
    driver.deployProcess(
      createUserTaskProcess(
        taskListeners = listOf(
          "update" to "#{changeTaskAttributes}"
        )
      )
    )

    // start
    val instance = driver.startProcessInstance()
    driver.assertProcessInstanceWaitsInUserTask(instance)
    verify(commandListGateway).sendToGateway(listOf(createTaskCommand()))

    // trigger the update event on the task
    taskService.saveTask(task().apply {
      name = "api name"
    })

    val updateCommand = updateTaskCommand()
    verify(commandListGateway).sendToGateway(listOf(updateCommand))

    verifyNoMoreInteractions(commandListGateway)
  }

  /**
   * The process is started and wait in a user task.
   * The update command is send after the TX commit.
   */
  @Test
  fun `assigns task via API with assignment listener`() {

    // deploy
    driver.deployProcess(
      createUserTaskProcess(

        candidateUsers = "fozzy",
        taskListeners = listOf(
          "assignment" to "#{setAssigneePiggy}"
        )
      )
    )

    // start
    val instance = driver.startProcessInstance()
    driver.assertProcessInstanceWaitsInUserTask(instance)
    verify(commandListGateway).sendToGateway(listOf(createTaskCommand(candidateUsers = setOf("fozzy"))))

    // trigger the update event on the task
    taskService.setAssignee(task().id, "kermit") // but the listener will set it back to piggy!

    verify(commandListGateway).sendToGateway(listOf(AssignTaskCommand(task().id, assignee = "piggy")))

    verifyNoMoreInteractions(commandListGateway)
  }

  /**
   * The process is started and wait in a user task.
   * The assign command is send after the TX commit.
   */
  @Test
  fun `(re) assigns user via API`() {

    // deploy
    driver.deployProcess(
      createUserTaskProcess(

        candidateUsers = "piggy, kermit",
        taskListeners = listOf(
          "create" to "#{setAssigneePiggy}" // will add a listener setting assignee to piggy on task creation.
        )
      )
    )

    // start
    val instance = driver.startProcessInstance()
    driver.assertProcessInstanceWaitsInUserTask(instance)
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
    driver.deployProcess(
      createUserTaskProcess(
        additionalUserTask = false,
        candidateGroups = "$muppets,$lords,$peasants"
      )
    )

    // start
    val instance = driver.startProcessInstance()
    driver.assertProcessInstanceWaitsInUserTask(instance)
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
    verify(commandListGateway).sendToGateway(listOf(deleteGroups, addGroups))
  }

  /**
   * The process is started and wait in a user task.
   * The candidate group change commands is sent after the TX commit.
   */
  @Test
  fun `changes candidate groups and further command attributes via API`() {

    val muppets = "muppets"
    val lords = "lords"
    val peasants = "peasants"
    val avengers = "avengers"
    val dwarfs = "dwarfs"

    // deploy
    driver.deployProcess(
      createUserTaskProcess(
        additionalUserTask = false,
        candidateGroups = "$muppets,$lords,$peasants"
      )
    )

    // start
    val instance = driver.startProcessInstance()
    driver.assertProcessInstanceWaitsInUserTask(instance)
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
    verify(commandListGateway).sendToGateway(listOf(assign, deleteGroups, addGroups, updateAttributes))
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
    driver.deployProcess(
      createUserTaskProcess(
        candidateUsers = "$kermit,$piggy"
      )
    )

    // start
    val instance = driver.startProcessInstance()
    driver.assertProcessInstanceWaitsInUserTask(instance)
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
    verify(commandListGateway).sendToGateway(listOf(deleteUsers, addUsers))
  }


  /**
   * The process is started and waits in the user task. If the instance is deleted,
   * and the listeners are notified, the delete command is sent out with process
   * variables contained prior the deletion.
   */
  @Test
  fun `deletes on process instance deletion`() {

    val reason = "I-test"
    // deploy
    driver.deployProcess(createUserTaskProcess())

    // start
    val instance = driver.startProcessInstance()
    driver.assertProcessInstanceWaitsInUserTask(instance)
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

}

data class MyStructureWithSet(val name: String, val key: String, val value: Int, val set: Set<String> = setOf())
