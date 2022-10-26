package io.holunda.camunda.taskpool.model

import io.holunda.camunda.taskpool.api.business.CorrelationMap
import io.holunda.camunda.taskpool.api.task.SourceReference
import org.camunda.bpm.engine.variable.VariableMap
import java.util.*

/**
 * Command Model representation of the task.
 */
class Task {

  // anchor for static extension functions.
  companion object {
    // don't delete even if empty.
  }

  lateinit var id: String
  lateinit var sourceReference: SourceReference
  lateinit var taskDefinitionKey: String
  lateinit var payload: VariableMap

  var formKey: String? = null

  var assignee: String? = null
  var businessKey: String? = null
  lateinit var candidateUsers: Set<String>
  lateinit var candidateGroups: Set<String>
  lateinit var correlations: CorrelationMap
  var createTime: Date? = null
  var description: String? = null
  var dueDate: Date? = null

  var followUpDate: Date? = null
  var name: String? = null
  var owner: String? = null
  var priority: Int? = 0

  /**
   * Updates task with data supplied by event.
   * @param task task containing new attributes.
   */
  fun update(task: Task) {
    id = task.id
    taskDefinitionKey = task.taskDefinitionKey
    sourceReference = task.sourceReference
    name = task.name
    description = task.description
    priority = task.priority
    owner = task.owner
    dueDate = task.dueDate
    followUpDate = task.followUpDate
    businessKey = task.businessKey
    correlations = task.correlations
    payload = task.payload

    if (task.formKey != null) {
      this.formKey = task.formKey
    }
  }

}
