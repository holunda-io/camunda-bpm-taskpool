package io.holunda.polyflow.taskpool.collector.task

import dev.bpmcrafters.processengineapi.CommonRestrictions
import dev.bpmcrafters.processengineapi.Empty
import dev.bpmcrafters.processengineapi.task.SubscribeForTaskCmd
import dev.bpmcrafters.processengineapi.task.TaskInformation
import dev.bpmcrafters.processengineapi.task.TaskSubscription
import dev.bpmcrafters.processengineapi.task.TaskSubscriptionApi
import io.holunda.camunda.taskpool.api.task.AssignTaskCommand
import io.holunda.camunda.taskpool.api.task.CompleteTaskCommand
import io.holunda.camunda.taskpool.api.task.CreateTaskCommand
import io.holunda.camunda.taskpool.api.task.DeleteTaskCommand
import io.holunda.camunda.taskpool.api.task.UpdateAttributeTaskCommand
import io.holunda.polyflow.taskpool.collector.ProcessEngineApiTaskpoolCollectorProperties
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.context.ApplicationEventPublisher
import java.util.concurrent.CompletableFuture

class TaskEventCollectorServiceTest {

  private val taskSubscriptionApi: TaskSubscriptionApi = mock()
  private val applicationEventPublisher: ApplicationEventPublisher = mock()
  private val subscription: TaskSubscription = mock()
  private val subscriptionCaptor = argumentCaptor<SubscribeForTaskCmd>()
  private lateinit var service: TaskEventCollectorService

  @BeforeEach
  fun setUp() {
    whenever(taskSubscriptionApi.subscribeForTask(any())).thenReturn(CompletableFuture.completedFuture(subscription))
    whenever(taskSubscriptionApi.unsubscribe(any())).thenReturn(CompletableFuture.completedFuture(Empty))
    service = TaskEventCollectorService(
      processEngineApiTaskpoolCollectorProperties = ProcessEngineApiTaskpoolCollectorProperties(applicationName = "polyflow"),
      applicationEventPublisher = applicationEventPublisher,
      taskSubscriptionApi = taskSubscriptionApi
    )
    service.subscribe()
    verify(taskSubscriptionApi).subscribeForTask(subscriptionCaptor.capture())
  }

  @Test
  fun `subscribes directly to all user tasks and unsubscribes on shutdown`() {
    val command = subscriptionCaptor.firstValue

    assertThat(command.restrictions).isEmpty()
    assertThat(command.taskType.name).isEqualTo("USER")
    assertThat(command.taskDescriptionKey).isNull()
    assertThat(command.payloadDescription).isNull()

    service.unsubscribe()

    verify(taskSubscriptionApi).unsubscribe(org.mockito.kotlin.check {
      assertThat(it.subscription).isSameAs(subscription)
    })
  }

  @Test
  fun `maps task delivery reasons and termination to Taskpool commands`() {
    val command = subscriptionCaptor.firstValue

    command.action.accept(task(TaskInformation.CREATE), emptyMap())
    command.action.accept(task(TaskInformation.ASSIGN), emptyMap())
    command.action.accept(task(TaskInformation.UPDATE), emptyMap())
    command.action.accept(task(TaskInformation.COMPLETE), emptyMap())
    command.termination.accept(task(TaskInformation.DELETE))

    val events = argumentCaptor<Any>()
    verify(applicationEventPublisher, times(5)).publishEvent(events.capture())
    assertThat(events.allValues.map { it::class.java }).containsExactly(
      CreateTaskCommand::class.java,
      AssignTaskCommand::class.java,
      UpdateAttributeTaskCommand::class.java,
      CompleteTaskCommand::class.java,
      DeleteTaskCommand::class.java
    )
  }

  @Test
  fun `classifies an unlabelled delivery from collector state`() {
    val command = subscriptionCaptor.firstValue
    command.action.accept(task(null), emptyMap())
    command.action.accept(task(null, assignee = "kermit"), emptyMap())

    val events = argumentCaptor<Any>()
    verify(applicationEventPublisher, times(2)).publishEvent(events.capture())
    assertThat(events.allValues[0]).isInstanceOf(CreateTaskCommand::class.java)
    assertThat(events.allValues[1]).isInstanceOf(AssignTaskCommand::class.java)
  }

  private fun task(reason: String?, assignee: String? = null): TaskInformation = TaskInformation(
    taskId = "task-1",
    meta = buildMap {
      reason?.let { put(TaskInformation.REASON, it) }
      put(CommonRestrictions.ACTIVITY_ID, "approve")
      put("processInstanceId", "instance-1")
      put("executionId", "execution-1")
      put("processDefinitionId", "definition-1")
      put("processDefinitionKey", "definition")
      put("tenantId", "tenant")
      assignee?.let { put("taskAssignee", it) }
    }
  )
}
