package io.holunda.camunda.taskpool.enricher

import io.holunda.camunda.taskpool.api.task.AssignTaskCommand
import io.holunda.camunda.taskpool.api.task.CompleteTaskCommand
import io.holunda.camunda.taskpool.api.task.CreateTaskCommand
import io.holunda.camunda.taskpool.api.task.DeleteTaskCommand

/*
interface TaskCommandEnricher<T : TaskCommand> {
  fun enrich(command: T): T
}
*/
interface CreateCommandEnricher { //: TaskCommandEnricher<CreateTaskCommand> {
  fun enrich(command: CreateTaskCommand): CreateTaskCommand
}

interface CompleteCommandEnricher { //: TaskCommandEnricher<CompleteTaskCommand> {
  fun enrich(command: CompleteTaskCommand): CompleteTaskCommand
}

interface AssignCommandEnricher { //: TaskCommandEnricher<AssignTaskCommand> {
  fun enrich(command: AssignTaskCommand): AssignTaskCommand
}

interface DeleteCommandEnricher { // : TaskCommandEnricher<DeleteTaskCommand> {
  fun enrich(command: DeleteTaskCommand): DeleteTaskCommand
}

