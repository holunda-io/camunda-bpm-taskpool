package io.holunda.camunda.taskpool.api.task

import io.holunda.camunda.taskpool.api.business.WithCorrelations
import java.util.*

/**
 * Task command received from the Camunda Engine.
 */
interface EngineTaskCommand : WithTaskId, CamundaTaskEvent {
  /**
   * Used to order commands before sending in case multiple events are received from the engine for the same task in the same transaction.
   * Commands with lower order value are sent before commands with higher order value.
   */
  val order: Int
  /**
   * Name of the task.
   */
  val name: String?
  /**
   * Task description.
   */
  val description: String?
  /**
   * Create time.
   */
  val createTime: Date?
  /**
   * Task owner.
   */
  val owner: String?
  /**
   * Current assignee.
   */
  val assignee: String?
  /**
   * List of candidate users.
   */
  val candidateUsers: List<String>
  /**
   * List of candidate groups.
   */
  val candidateGroups: List<String>
  /**
   * Due date.
   */
  val dueDate: Date?
  /**
   * Follow-up date
   */
  val followUpDate: Date?
  /**
   * Priority.
   */
  val priority: Int?
}

/**
 * Enriched command received from Camunda Engine (with variables and correlations)
 */
interface EnrichedEngineTaskCommand : EngineTaskCommand, WithPayload, WithCorrelations, TaskIdentity
