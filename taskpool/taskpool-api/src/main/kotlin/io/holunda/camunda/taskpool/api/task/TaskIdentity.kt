package io.holunda.camunda.taskpool.api.task

interface TaskIdentity {
  val id: String
  val taskDefinitionKey: String
  val sourceReference: SourceReference
}
