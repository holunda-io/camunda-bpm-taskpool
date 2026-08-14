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
fun TaskInformation.asAssignCommand(): AssignTaskCommand = AssignTaskCommand(
  id = this.taskId,
  assignee = this.meta["taskAssignee"],
)

//TODO: C8 currently only sends update commands. So this will never update assignee or candidates
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

fun TaskInformation.asCompleteCommand(): CompleteTaskCommand = CompleteTaskCommand(
  id = this.taskId,
)

fun TaskInformation.asDeleteCommand(): DeleteTaskCommand = DeleteTaskCommand(
  id = this.taskId, deleteReason = null)

fun convertToUTCLocalDateTime(dateString: String?): Date? {
  if (dateString == null) return null
  val zonedDateTime =
    ZonedDateTime.parse(dateString).withZoneSameInstant(ZoneId.of("UTC"))
  return Date.from(zonedDateTime.toInstant())
}
