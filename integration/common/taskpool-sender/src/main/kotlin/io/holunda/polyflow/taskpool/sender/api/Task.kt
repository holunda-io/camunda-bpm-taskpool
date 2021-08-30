package io.holunda.polyflow.taskpool.sender.api

import io.holunda.camunda.taskpool.api.task.SourceReference
import org.camunda.bpm.engine.variable.VariableMap
import org.camunda.bpm.engine.variable.Variables
import java.util.*

/**
 * Represents a task.
 */
data class Task (
  /**
   * Task id.
   */
  val id: String,
  /**
   * Source reference, indicating why this task exists.
   */
  val sourceReference: SourceReference,
  /**
   * Task definition key aka task type.
   */
  val taskDefinitionKey: String,
  /**
   * Form key used to create a task URL.
   */
  val formKey: String? = null,
  /**
   * Business key of the underlying process instance.
   */
  val businessKey: String? = null,
  /**
   * Task payload.
   */
  val payload: VariableMap = Variables.createVariables(),
  /**
   * Username of the assigned user.
   */
  val assignee: String? = null,
  /**
   * Set of usernames marked as candidates.
   */
  val candidateUsers: Set<String> = setOf(),
  /**
   * Set of user groups marked as candidates.
   */
  val candidateGroups: Set<String> = setOf(),
  /**
   * Creation timestamp.
   */
  val createTime: Date? = null,
  /**
   * Task description.
   */
  val description: String? = null,
  /**
   * Task due date.
   */
  val dueDate: Date? = null,
  /**
   * Task follow-up date.
   */
  val followUpDate: Date? = null,
  /**
   * Task name.
   */
  val name: String? = null,
  /**
   * Task owner (assigned user).
   */
  val owner: String? = null,
  /**
   * Task priority.
   */
  val priority: Int? = null
)
