package io.holunda.polyflow.taskpool.itest.txjob

import io.holunda.camunda.taskpool.api.task.*
import io.holunda.polyflow.taskpool.EnableCamundaTaskpoolCollector
import io.holunda.polyflow.taskpool.EnableTaskpoolSender
import io.holunda.polyflow.taskpool.itest.TestDriver
import io.holunda.polyflow.taskpool.itest.TestDriver.Companion.createUserTaskProcess
import io.holunda.polyflow.taskpool.sender.gateway.CommandListGateway
import org.assertj.core.api.Assertions
import org.awaitility.Awaitility
import org.axonframework.commandhandling.gateway.CommandGateway
import org.camunda.bpm.engine.ManagementService
import org.camunda.bpm.engine.RepositoryService
import org.camunda.bpm.engine.RuntimeService
import org.camunda.bpm.engine.delegate.TaskListener
import org.camunda.bpm.engine.impl.persistence.entity.MessageEntity
import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.*
import org.camunda.bpm.engine.variable.Variables
import org.camunda.bpm.spring.boot.starter.annotation.EnableProcessApplication
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.mockito.kotlin.mock
import org.mockito.kotlin.verifyNoMoreInteractions
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.support.TransactionTemplate
import java.util.*
import java.util.concurrent.TimeUnit


@SpringBootTest(classes = [TaskTxJobSenderITest.TaskTxJobSenderTestApplication::class], webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ActiveProfiles("txjob-sender-itest")
@DirtiesContext
@Transactional
internal class TaskTxJobSenderITest {

  @MockBean
  lateinit var commandListGateway: CommandListGateway

  @Autowired
  lateinit var repositoryService: RepositoryService

  @Autowired
  lateinit var runtimeService: RuntimeService

  @Autowired
  lateinit var managementService: ManagementService

  @Autowired
  lateinit var txTemplate: TransactionTemplate

  lateinit var createCommand: CreateTaskCommand

  private val driver: TestDriver by lazy {
    TestDriver(repositoryService, runtimeService)
  }

  @BeforeEach
  fun `setup tx template`() {
    txTemplate.propagationBehavior = Propagation.REQUIRES_NEW.value()
  }

  @Test
  fun `creates task in process`() {

    doInTransaction {
      // deploy
      driver.deployProcess(
        createUserTaskProcess()
      )

      // start
      val instance = driver.startProcessInstance()
      // instance is started
      assertThat(instance).isStarted
      // user task
      driver.assertProcessInstanceWaitsInUserTask(instance)

      verifyNoMoreInteractions(commandListGateway)

      createCommand = TestDriver.createTaskCommand()
    }

    assertAndExecuteCommandSendingJob()

    verify(commandListGateway).sendToGateway(
      listOf(createCommand)
    )
  }

  /**
   * The process is started and waits in a user task. The user task has a task listener that changes some local process variables on create.
   * The create command should contain the local variables.
   */
  @Test
  @Disabled("Find out why the local listener update always gt into the next TX and how to deal with it")
  fun `updates variables with create listener`() {

    doInTransaction {
      // deploy
      driver.deployProcess(
        createUserTaskProcess(
          taskListeners = listOf(
            "create" to "#{setTaskLocalVariables}"
          )
        )
      )

      // start
      val instance = driver.startProcessInstance(variables = Variables.createVariables().putValue("overriddenVariable", "global-value"))
      driver.assertProcessInstanceWaitsInUserTask(instance)

      verifyNoMoreInteractions(commandListGateway)

      createCommand = TestDriver.createTaskCommand(
        variables = Variables.createVariables()
          .putValue("taskLocalOnlyVariable", "only-value")
          .putValue("overriddenVariable", "local-value")
      )
    }

    assertAndExecuteCommandSendingJob()

    verify(commandListGateway).sendToGateway(
      listOf(createCommand)
    )

  }

  private fun assertAndExecuteCommandSendingJob() {
    doInTransaction {
      val jobs = managementService.createJobQuery().list()
      Assertions.assertThat(jobs).hasSize(1)
      Assertions.assertThat(jobs[0]).isInstanceOf(MessageEntity::class.java)
      Assertions.assertThat((jobs[0] as MessageEntity).jobHandlerType).isEqualTo("polyflow-engine-task-command-sending")

      Awaitility.waitAtMost(3, TimeUnit.SECONDS).untilAsserted {
        try {
          execute(jobs[0])
        } catch (e: Exception) {
          // brute force preventing Optimistic locking exception, IllegalStateException (job doesn't exist)
        }
        Assertions.assertThat(managementService.createJobQuery().count()).isEqualTo(0)
      }
    }
  }

  private fun doInTransaction(operation: Runnable) {
    txTemplate.execute<Any> {
      operation.run()
      null
    }

  }

  @SpringBootApplication
  @EnableProcessApplication
  @EnableCamundaTaskpoolCollector
  @EnableTaskpoolSender
  class TaskTxJobSenderTestApplication {

    @Bean
    @Primary
    fun testTxJobAxonCommandGateway(): CommandGateway = mock()

    /**
     * A task listener that sets some local variables.
     */
    @Bean
    fun setTaskLocalVariables() = TaskListener { delegateTask ->
      delegateTask.setVariableLocal("taskLocalOnlyVariable", "only-value")
      delegateTask.setVariableLocal("overriddenVariable", "local-value")
    }

  }
}
