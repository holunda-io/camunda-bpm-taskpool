package io.holunda.polyflow.taskpool.itest.txjob

import io.holunda.camunda.taskpool.api.task.*
import io.holunda.polyflow.taskpool.EnableCamundaTaskpoolCollector
import io.holunda.polyflow.taskpool.EnableTaskpoolSender
import io.holunda.polyflow.taskpool.itest.TestDriver
import io.holunda.polyflow.taskpool.itest.TestDriver.Companion.createUserTaskProcess
import io.holunda.polyflow.taskpool.sender.gateway.CommandListGateway
import org.assertj.core.api.Assertions
import org.axonframework.commandhandling.gateway.CommandGateway
import org.camunda.bpm.engine.ManagementService
import org.camunda.bpm.engine.RepositoryService
import org.camunda.bpm.engine.RuntimeService
import org.camunda.bpm.engine.TaskService
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor
import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.*
import org.camunda.bpm.spring.boot.starter.annotation.EnableProcessApplication
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
import org.springframework.test.annotation.Commit
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.transaction.AfterTransaction
import org.springframework.transaction.annotation.Transactional
import java.util.*


@Disabled("Understand how to test this")
@SpringBootTest(classes = [TaskTxJobSenderITest.TaskTxJobSenderTestApplication::class], webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ActiveProfiles("txjob-sender-itest")
@DirtiesContext
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
  lateinit var taskServiceService: TaskService


  @Autowired
  lateinit var commandExecutor: CommandExecutor


  private val driver: TestDriver by lazy {
    TestDriver(repositoryService, runtimeService)
  }

  @Test
  @Transactional
  @Commit
  fun `creates task in process`() {
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

  }

  @AfterTransaction
  fun `after all`() {
    val jobs = managementService.createJobQuery().list()
    Assertions.assertThat(jobs).hasSize(1)
  }


  @SpringBootApplication
  @EnableProcessApplication
  @EnableCamundaTaskpoolCollector
  @EnableTaskpoolSender
  class TaskTxJobSenderTestApplication {

    @Bean
    @Primary
    fun testTxJobAxonCommandGateway(): CommandGateway = mock()
  }
}
