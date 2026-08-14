package io.holunda.polyflow.taskpool.collector.task

import dev.bpmcrafters.processengineapi.task.TaskInformation
import dev.bpmcrafters.processengineapi.task.SubscribeForTaskCmd
import dev.bpmcrafters.processengineapi.task.TaskSubscription
import dev.bpmcrafters.processengineapi.task.TaskSubscriptionApi
import dev.bpmcrafters.processengineapi.task.TaskTerminationHandler
import dev.bpmcrafters.processengineapi.task.TaskType
import dev.bpmcrafters.processengineapi.task.UnsubscribeFromTaskCmd
import dev.bpmcrafters.processengineapi.CommonRestrictions
import io.github.oshai.kotlinlogging.KotlinLogging
import io.holunda.polyflow.taskpool.asAssignCommand
import io.holunda.polyflow.taskpool.asCompleteCommand
import io.holunda.polyflow.taskpool.asCreatedCommand
import io.holunda.polyflow.taskpool.asDeleteCommand
import io.holunda.polyflow.taskpool.asUpdateCommand
import io.holunda.polyflow.taskpool.collector.ProcessEngineApiTaskpoolCollectorProperties
import jakarta.annotation.PreDestroy
import jakarta.annotation.PostConstruct
import org.springframework.context.ApplicationEventPublisher
import java.util.concurrent.ConcurrentHashMap

private val logger = KotlinLogging.logger {}

/**
 * Subscribes to Process Engine API user-task delivery and emits Taskpool commands.
 */
class TaskEventCollectorService(
  val processEngineApiTaskpoolCollectorProperties: ProcessEngineApiTaskpoolCollectorProperties, // must not be private to access in conditions of event handlers
  private val applicationEventPublisher: ApplicationEventPublisher,
  private val taskSubscriptionApi: TaskSubscriptionApi
) {

  private val knownTasks = ConcurrentHashMap<String, TaskInformation>()
  private lateinit var subscription: TaskSubscription

  /**
   * Static constants.
   */
  companion object {
    const val NAME = "taskEventCollectorService"
  }

  @PostConstruct
  fun subscribe() {
    subscription = taskSubscriptionApi.subscribeForTask(
      SubscribeForTaskCmd(
        restrictions = emptyMap(),
        taskType = TaskType.USER,
        taskDescriptionKey = null,
        payloadDescription = null,
        action = { task, payload -> onTaskDelivery(task, payload) },
        termination = TaskTerminationHandler { task -> onTaskTermination(task) }
      )
    ).join()
  }

  @PreDestroy
  fun unsubscribe() {
    if (::subscription.isInitialized) {
      taskSubscriptionApi.unsubscribe(UnsubscribeFromTaskCmd(subscription)).join()
    }
  }

  private fun onTaskDelivery(task: TaskInformation, payload: Map<String, Any?>) {
    when (deliveryReason(task)) {
      TaskInformation.CREATE -> applicationEventPublisher.publishEvent(task.asCreatedCommand(processEngineApiTaskpoolCollectorProperties.applicationName, payload))
      TaskInformation.ASSIGN -> applicationEventPublisher.publishEvent(task.asAssignCommand())
      TaskInformation.UPDATE -> applicationEventPublisher.publishEvent(task.asUpdateCommand(processEngineApiTaskpoolCollectorProperties.applicationName, payload))
      TaskInformation.COMPLETE -> applicationEventPublisher.publishEvent(task.asCompleteCommand())
      else -> logger.warn { "Received unexpected task delivery for task ${task.taskId}." }
    }
    knownTasks[task.taskId] = task
  }

  private fun onTaskTermination(task: TaskInformation) {
    knownTasks.remove(task.taskId)
    applicationEventPublisher.publishEvent(task.asDeleteCommand())
  }

  fun findTaskId(executionId: String): String? =
    knownTasks.values.firstOrNull { it.meta[CommonRestrictions.EXECUTION_ID] == executionId }?.taskId

  private fun deliveryReason(task: TaskInformation): String {
    val reason = task.meta[TaskInformation.REASON]
    if (reason == TaskInformation.CREATE || reason == TaskInformation.ASSIGN || reason == TaskInformation.UPDATE || reason == TaskInformation.COMPLETE) {
      return reason
    }

    val previous = knownTasks[task.taskId] ?: return TaskInformation.CREATE
    return if (assignmentChanged(previous, task)) TaskInformation.ASSIGN else TaskInformation.UPDATE
  }

  private fun assignmentChanged(previous: TaskInformation, current: TaskInformation): Boolean =
    listOf("taskAssignee", "candidateUsers", "candidateGroups").any { key -> previous.meta[key] != current.meta[key] }
}
