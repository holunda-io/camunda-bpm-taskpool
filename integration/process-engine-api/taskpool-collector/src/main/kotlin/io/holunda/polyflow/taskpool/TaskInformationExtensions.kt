package io.holunda.polyflow.taskpool

import dev.bpmcrafters.processengineapi.CommonRestrictions
import dev.bpmcrafters.processengineapi.task.TaskInformation
import io.holunda.camunda.taskpool.api.task.AssignTaskCommand
import io.holunda.camunda.taskpool.api.task.CompleteTaskCommand
import io.holunda.camunda.taskpool.api.task.CreateTaskCommand
import io.holunda.camunda.taskpool.api.task.DeleteTaskCommand
import io.holunda.camunda.taskpool.api.task.ProcessReference
import io.holunda.camunda.taskpool.api.task.SourceReference
import io.holunda.camunda.taskpool.api.task.UpdateAttributeTaskCommand
import org.camunda.bpm.engine.variable.Variables
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*

/**
 * Creates the Taskpool source reference represented by this Process Engine API task.
 *
 * @param applicationName name of the application that collects the task.
 * @return the process reference used as the task's source reference.
 */
fun TaskInformation.sourceReference(applicationName: String): SourceReference {
  return ProcessReference(
    instanceId = this.meta["processInstanceId"].toString(),
    executionId = this.meta["executionId"].toString(), //TODO
    definitionId = this.meta["processDefinitionId"].toString(),
    definitionKey = this.meta["processDefinitionKey"].toString(),
    name = this.meta["processDefinitionKey"].toString(),
    applicationName = applicationName,
    tenantId = this.meta["tenantId"].toString(),
  )
}

/**
 * Converts this Process Engine API task into a Taskpool create command.
 *
 * @param applicationName name of the application that collects the task.
 * @param payload process variables delivered with the task.
 * @return a command that creates the corresponding Taskpool task.
 */
fun TaskInformation.asCreatedCommand(applicationName: String, payload: Map<String, Any?>): CreateTaskCommand = CreateTaskCommand(
  id = this.taskId,
  assignee = this.meta["taskAssignee"],
  candidateGroups = this.meta["candidateGroups"]?.split(",")?.toSet() ?: emptySet(),
  candidateUsers = this.meta["candidateUsers"]?.split(",")?.toSet() ?: emptySet(),
  createTime = convertToUTCLocalDateTime(this.meta["creationDate"]),
  description = this.meta["taskDescription"],
  dueDate = this.meta["dueDate"]?.let { convertToUTCLocalDateTime(it) },
  followUpDate = this.meta["followUpDate"]?.takeUnless { it.isBlank() }?.let { convertToUTCLocalDateTime(it) },
  eventName = "Create",
  name = this.meta["taskName"] ?: "unknwon task",
  priority = this.meta["taskPriority"]?.toIntOrNull(),
  formKey = this.meta["formKey"],
  taskDefinitionKey = this.meta[CommonRestrictions.ACTIVITY_ID]!!,
  businessKey = this.meta[CommonRestrictions.BUSINESS_KEY],
  sourceReference = this.sourceReference(applicationName),
  payload = Variables.fromMap(payload),
)

// TODO: Assign now means that any assignment has changed, either assigne, candidate user or candidate group
/**
 * Converts this Process Engine API task into a Taskpool assignment command.
 *
 * @return a command containing the task identifier and current assignee.
 */
fun TaskInformation.asAssignCommand(): AssignTaskCommand = AssignTaskCommand(
  id = this.taskId,
  assignee = this.meta["taskAssignee"],
)

//TODO: C8 currently only sends update commands. So this will never update assignee or candidates
/**
 * Converts this Process Engine API task into a Taskpool attribute-update command.
 *
 * @param applicationName name of the application that collects the task.
 * @param payload process variables delivered with the task.
 * @return a command that updates the corresponding Taskpool task attributes.
 */
fun TaskInformation.asUpdateCommand(applicationName: String, payload: Map<String, Any?>): UpdateAttributeTaskCommand = UpdateAttributeTaskCommand(
  id = this.taskId,
  description = this.meta["taskDescription"],
  dueDate = this.meta["dueDate"]?.let { convertToUTCLocalDateTime(it) },
  followUpDate = this.meta["followUpDate"]?.filter { it.toString().isBlank() }?.let { convertToUTCLocalDateTime(it) },
  eventName = "Create",
  name = this.meta["taskName"],
  priority = this.meta["taskPriority"]?.toIntOrNull(),
  taskDefinitionKey = this.meta[CommonRestrictions.ACTIVITY_ID]!!,
  businessKey = this.meta[CommonRestrictions.BUSINESS_KEY],
  sourceReference = this.sourceReference(applicationName),
  payload = Variables.fromMap(payload),
  owner = null,
)

/**
 * Converts this Process Engine API task into a Taskpool completion command.
 *
 * @return a command that completes the corresponding Taskpool task.
 */
fun TaskInformation.asCompleteCommand(): CompleteTaskCommand = CompleteTaskCommand(
  id = this.taskId,
)

/**
 * Converts this Process Engine API task into a Taskpool deletion command.
 *
 * @return a command that deletes the corresponding Taskpool task.
 */
fun TaskInformation.asDeleteCommand(): DeleteTaskCommand = DeleteTaskCommand(
  id = this.taskId, deleteReason = null)

/**
 * Parses an ISO-8601 date-time string and expresses it as an instant in UTC.
 *
 * @param dateString the date-time string to parse, or `null`.
 * @return the corresponding instant as a [Date], or `null` when [dateString] is `null`.
 */
fun convertToUTCLocalDateTime(dateString: String?): Date? {
  if (dateString == null) return null
  val zonedDateTime =
    ZonedDateTime.parse(dateString).withZoneSameInstant(ZoneId.of("UTC"))
  return Date.from(zonedDateTime.toInstant())
}
