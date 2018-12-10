package io.holunda.camunda.taskpool.api.task

import io.holunda.camunda.taskpool.api.business.WithCorrelations
import java.util.*

/**
 * Task command received from the Camunda Engine.
 */
interface EngineTaskCommand : TaskIdentity, CamundaTaskEvent {
  val name: String?
  val description: String?
  val formKey: String?
  val createTime: Date?
  val owner: String?
  val assignee: String?
  val candidateUsers: List<String>
  val candidateGroups: List<String>
  val dueDate: Date?
  val followUpDate: Date?
  val priority: Int?
}

/**
 * Enriched command received from Camunda Engine (with variables and corellations)
 */
interface EnrichedEngineTaskCommand : EngineTaskCommand, WithPayload, WithCorrelations
