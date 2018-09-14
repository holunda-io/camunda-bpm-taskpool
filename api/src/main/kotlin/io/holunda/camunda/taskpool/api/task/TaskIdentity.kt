package io.holunda.camunda.taskpool.api.task

interface TaskIdentity {
  val id: String
  val processReference: ProcessReference?
  val caseReference: CaseReference?
  val taskDefinitionKey: String
}

data class ProcessReference(
  val processInstanceId: String,
  // TODO: maybe rename
  val executionId: String,
  val processDefinitionId: String,
  val processDefinitionKey: String
)

data class CaseReference(
  val caseInstanceId: String,
  val caseExecutionId: String,
  val caseDefinitionId: String,
  val caseDefinitionKey: String
)

