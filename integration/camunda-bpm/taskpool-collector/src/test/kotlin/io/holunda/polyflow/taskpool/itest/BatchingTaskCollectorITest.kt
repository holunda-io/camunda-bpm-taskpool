package io.holunda.polyflow.taskpool.itest

import io.holunda.camunda.taskpool.api.task.*
import io.holunda.polyflow.taskpool.itest.TestDriver.Companion.createTaskCommand
import io.holunda.polyflow.taskpool.itest.TestDriver.Companion.createUserTaskProcess
import io.holunda.polyflow.taskpool.itest.TestDriver.Companion.updateTaskCommand
import io.holunda.polyflow.taskpool.sender.gateway.CommandListGateway
import org.camunda.bpm.engine.RepositoryService
import org.camunda.bpm.engine.RuntimeService
import org.camunda.bpm.engine.TaskService
import org.camunda.bpm.engine.impl.interceptor.Command
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor
import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.*
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.MOCK
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import java.util.*

@SpringBootTest(
  classes = [CollectorTestApplication::class],
  webEnvironment = MOCK,
  properties = ["polyflow.integration.sender.task.batch-commands=true"]
)
@ActiveProfiles("collector-itest")
@DirtiesContext
@Disabled("FIXME: find out why this test runs in isolation only.")
internal class BatchingTaskCollectorITest {

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

  val driver: TestDriver by lazy {
    TestDriver(repositoryService, runtimeService)
  }

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
    driver.deployProcess(
      createUserTaskProcess(
        additionalUserTask = false, candidateGroups = "$muppets,$lords,$peasants"
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
    driver.deployProcess(
      createUserTaskProcess(
        additionalUserTask = false, candidateGroups = "$muppets,$lords,$peasants"
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
    verify(commandListGateway).sendToGateway(
      listOf(BatchCommand(id = task().id, commands = listOf(assign, deleteGroups, addGroups, updateAttributes)))
    )

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
    verify(commandListGateway).sendToGateway(
      listOf(BatchCommand(id = task().id, commands = listOf(deleteUsers, addUsers)))
    )
  }
}
