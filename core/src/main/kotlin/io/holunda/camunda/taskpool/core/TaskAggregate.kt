package io.holunda.camunda.taskpool.core

import io.holunda.camunda.taskpool.api.task.CreateTaskCommand
import io.holunda.camunda.taskpool.api.task.TaskCreatedEvent
import mu.KLogging
import org.axonframework.commandhandling.CommandHandler
import org.axonframework.commandhandling.model.AggregateIdentifier
import org.axonframework.commandhandling.model.AggregateLifecycle
import org.axonframework.eventsourcing.EventSourcingHandler
import org.axonframework.spring.stereotype.Aggregate


@Aggregate
class TaskAggregate {

  companion object : KLogging()

  @AggregateIdentifier
  private lateinit var id: String

  @CommandHandler
  constructor(command: CreateTaskCommand) {
    logger.info { "Task command received $command" }
    AggregateLifecycle.apply(TaskCreatedEvent(
      id = command.id
    ))
  }

  @EventSourcingHandler
  fun on(event: TaskCreatedEvent) {
    this.id = event.id
    logger.info { "Created task $event" }
  }
}
