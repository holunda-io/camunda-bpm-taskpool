package io.holunda.polyflow.taskpool

import org.apache.commons.lang3.reflect.FieldUtils
import org.camunda.bpm.engine.impl.persistence.entity.PropertyChange
import org.camunda.bpm.engine.impl.persistence.entity.TaskEntity

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