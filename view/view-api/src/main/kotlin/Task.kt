package io.holunda.polyflow.view

import io.holunda.camunda.taskpool.api.business.*
import io.holunda.camunda.taskpool.api.task.SourceReference
import io.holunda.camunda.taskpool.api.task.TaskIdentity
import io.holunda.camunda.taskpool.api.task.WithPayload
import org.camunda.bpm.engine.variable.VariableMap
import org.camunda.bpm.engine.variable.Variables
import java.time.Instant

/**
 * User task.
 */
data class Task(
  override val id: String,
  override val sourceReference: SourceReference,
  override val taskDefinitionKey: String,
  override val payload: VariableMap = Variables.createVariables(),
  override val correlations: CorrelationMap = newCorrelations(),
  override val businessKey: String? = null,
  val name: String? = null,
  val description: String? = null,
  val formKey: String? = null,
  val priority: Int? = 0,
  val createTime: Instant? = null,
  val candidateUsers: Set<String> = setOf(),
  val candidateGroups: Set<String> = setOf(),
  val assignee: String? = null,
  val owner: String? = null,
  val dueDate: Instant? = null,
  val followUpDate: Instant? = null,
  val deleted: Boolean = false
) : TaskIdentity, WithPayload, WithCorrelations {

  val correlationIdentities: Set<String> = correlations.map { dataIdentityString(it.key, it.value as EntryId) }.toSet()

}
