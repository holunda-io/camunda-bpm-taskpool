package io.holunda.camunda.taskpool.enricher

import io.holunda.camunda.taskpool.api.task.AssignTaskCommand
import io.holunda.camunda.taskpool.api.task.CompleteTaskCommand
import io.holunda.camunda.taskpool.api.task.CreateTaskCommand
import io.holunda.camunda.taskpool.api.task.DeleteTaskCommand
import org.springframework.context.event.EventListener

class EmptyCreateCommandEnricher : CreateCommandEnricher {
  @EventListener(condition = "#command.enriched == false")
  override fun enrich(command: CreateTaskCommand): CreateTaskCommand = command.apply { enriched = true }
}

class EmptyCompleteCommandEnricher : CompleteCommandEnricher {
  @EventListener(condition = "#command.enriched == false")
  override fun enrich(command: CompleteTaskCommand): CompleteTaskCommand = command.apply { enriched = true }
}

class EmptyDeleteCommandEnricher : DeleteCommandEnricher {
  @EventListener(condition = "#command.enriched == false")
  override fun enrich(command: DeleteTaskCommand): DeleteTaskCommand = command.apply { enriched = true }
}

class EmptyAssignCommandEnricher : AssignCommandEnricher {
  @EventListener(condition = "#command.enriched == false")
  override fun enrich(command: AssignTaskCommand): AssignTaskCommand = command.apply { enriched = true }
}

