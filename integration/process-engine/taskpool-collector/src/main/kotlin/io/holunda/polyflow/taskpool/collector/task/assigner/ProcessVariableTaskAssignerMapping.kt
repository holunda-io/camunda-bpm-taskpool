package io.holunda.polyflow.taskpool.collector.task.assigner

import org.camunda.bpm.engine.variable.VariableMap

/**
 * A mapping defining the names of process variables used for assignment.
 */
data class ProcessVariableTaskAssignerMapping(
  val assignee: String?,
  val candidateUsers: String?,
  val candidateGroups: String?
) {
  /**
   * Loads assignment from variables.
   */
  fun loadAssignmentFromVariables(variables: VariableMap): Assignment =
    Assignment(
      assignee = if (assignee != null && variables.containsKey(assignee)) {
        variables[assignee].asStringValue()
      } else {
        null
      },
      candidateUsers = if (candidateUsers != null && variables.containsKey(candidateUsers)) {
        variables[candidateUsers].asSetValue()
      } else {
        setOf()
      },
      candidateGroups = if (candidateGroups != null && variables.containsKey(candidateGroups)) {
        variables[candidateGroups].asSetValue()
      } else {
        setOf()
      }
    )

  /**
   * Assignment information.
   */
  data class Assignment(
    /**
     * Assignee.
     */
    val assignee: String?,
    /**
     * Candidate users.
     */
    val candidateUsers: Set<String>,
    /**
     * Candidate groups.
     */
    val candidateGroups: Set<String>
  )
}

/**
 * Variable value to set or empty set.
 */
@Suppress("UNCHECKED_CAST")
fun Any?.asSetValue() =
  when (this) {
    is Collection<*> -> (this as Collection<String>).toSet()
    is String -> this.split(",").map { it.trim() }.toSet()
    else -> setOf()
  }

/**
 * Variable value to string or null.
 */
fun Any?.asStringValue() =
  when (this) {
    is String -> this
    else -> null
  }

