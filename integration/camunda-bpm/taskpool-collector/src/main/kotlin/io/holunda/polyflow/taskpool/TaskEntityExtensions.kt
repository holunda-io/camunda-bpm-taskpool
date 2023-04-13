package io.holunda.polyflow.taskpool

import io.holunda.camunda.taskpool.api.task.CreateTaskCommand
import org.apache.commons.lang3.reflect.FieldUtils
import org.camunda.bpm.engine.delegate.TaskListener
import org.camunda.bpm.engine.impl.persistence.entity.PropertyChange
import org.camunda.bpm.engine.impl.persistence.entity.TaskEntity
import org.camunda.bpm.engine.task.IdentityLinkType

/**
 * Reads identity changes from the task entity.
 */
@Suppress("UNCHECKED_CAST")
fun TaskEntity.getIdentityLinkChanges(): List<PropertyChange> {
  // reads protected field.
  return FieldUtils.readField(this, "identityLinkChanges", true) as List<PropertyChange>
}

/**
 * Flag denoting that the change of the task is an assignee only.
 */
fun TaskEntity.isAssigneeChange(): Boolean =
  this.propertyChanges.size == 1 && this.propertyChanges.containsKey("assignee")

/**
 * Checks if the task entity has changed properties.
 */
fun TaskEntity.hasChangedProperties(): Boolean = this.propertyChanges.isNotEmpty()

/**
 * Creates a new CreateTaskCommand from this task entity. The resulting command has no correlations and is not enriched.
 * @param applicationName application name.
 * @return create task command.
 */
fun TaskEntity.asCreateCommand(applicationName: String) = CreateTaskCommand(
    id = this.id,
    assignee = this.assignee,
    candidateGroups = this.candidates.filter { it.groupId != null }.map { it.groupId }.toSet(),
    candidateUsers = this.candidates.filter { it.userId != null && it.type == IdentityLinkType.CANDIDATE }.map { it.userId }.toSet(),
    createTime = this.createTime,
    description = this.description,
    dueDate = this.dueDate,
    followUpDate = this.followUpDate,
    eventName = TaskListener.EVENTNAME_CREATE,
    name = this.name,
    owner = this.owner,
    priority = this.priority,
    formKey = this.formKey(),
    taskDefinitionKey = this.taskDefinitionKey,
    businessKey = this.execution.businessKey,
    sourceReference = this.sourceReference(applicationName)
  )
