package io.holunda.camunda.taskpool.enricher

import io.holunda.camunda.taskpool.api.task.AssignTaskCommand
import io.holunda.camunda.taskpool.api.task.CompleteTaskCommand
import io.holunda.camunda.taskpool.api.task.CreateTaskCommand
import io.holunda.camunda.taskpool.api.task.DeleteTaskCommand


interface CreateCommandEnricher {
  fun enrich(command: CreateTaskCommand): CreateTaskCommand
}

interface CompleteCommandEnricher {
  fun enrich(command: CompleteTaskCommand): CompleteTaskCommand
}

interface AssignCommandEnricher {
  fun enrich(command: AssignTaskCommand): AssignTaskCommand
}

interface DeleteCommandEnricher {
  fun enrich(command: DeleteTaskCommand): DeleteTaskCommand
}

