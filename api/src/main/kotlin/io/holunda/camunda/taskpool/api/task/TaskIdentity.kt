package io.holunda.camunda.taskpool.api.task

interface TaskIdentity {
  val id: String
  val processReference: ProcessReference?
  val caseReference: CaseReference?
  val taskDefinitionKey: String
}
