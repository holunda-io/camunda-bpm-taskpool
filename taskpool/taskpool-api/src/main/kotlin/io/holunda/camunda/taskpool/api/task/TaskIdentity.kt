package io.holunda.camunda.taskpool.api.task

interface TaskIdentity : WithTaskId {
  val taskDefinitionKey: String
  val sourceReference: SourceReference
}
