package io.holunda.camunda.taskpool.api.task

import io.holunda.camunda.taskpool.api.business.WithCorrelations
import java.util.*

/**
 * Task command received from the Camunda Engine.
 */
interface EngineTaskCommand : WithTaskId, CamundaTaskEvent {
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
