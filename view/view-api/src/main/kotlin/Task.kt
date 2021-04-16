package io.holunda.camunda.taskpool.view

import io.holunda.camunda.taskpool.api.business.*
import io.holunda.camunda.taskpool.api.task.*
import io.holunda.camunda.taskpool.api.task.WithPayload
import org.camunda.bpm.engine.variable.VariableMap
import org.camunda.bpm.engine.variable.Variables
import java.util.*

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
  val createTime: Date? = null,
  val candidateUsers: Set<String> = setOf(),
  val candidateGroups: Set<String> = setOf(),
  val assignee: String? = null,
  val owner: String? = null,
  val dueDate: Date? = null,
  val followUpDate: Date? = null,
  val deleted: Boolean = false
) : TaskIdentity, WithPayload, WithCorrelations {

  val correlationIdentities by lazy {
    correlations.map { dataIdentityString(it.key, it.value as EntryId) }.toSet()
  }

}

