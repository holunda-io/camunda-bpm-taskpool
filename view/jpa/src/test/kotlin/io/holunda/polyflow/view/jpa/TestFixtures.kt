package io.holunda.polyflow.view.jpa

import io.holunda.polyflow.view.jpa.process.SourceReferenceEmbeddable
import io.holunda.polyflow.view.jpa.task.TaskEntity
import java.util.*

data class Pojo(
  val attribute1: String,
  val attribute2: Date
)


fun emptyTask() = TaskEntity(
  taskId = "id-4711",
  taskDefinitionKey = "task.def.0815",
  name = "task name",
  priority = 50,
  sourceReference = processReference(),
  payload = null
)

fun processReference() = SourceReferenceEmbeddable(
  instanceId = "instance-1283947",
  executionId = "execution-4568789",
  definitionId = "12313-12343-34244-23423:13",
  definitionKey = "12313-12343-34244-23423",
  name = "process",
  applicationName = "test-application",
  sourceType = "PROCESS"
)
