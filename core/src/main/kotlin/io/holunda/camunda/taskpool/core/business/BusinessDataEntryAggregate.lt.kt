package io.holunda.camunda.taskpool.core.business

import io.holunda.camunda.taskpool.api.business.CreateDataEntryCommand
import io.holunda.camunda.taskpool.api.business.DataEntryCreatedEvent
import io.holunda.camunda.taskpool.api.business.EntryId
import io.holunda.camunda.taskpool.api.business.EntryType
import org.axonframework.commandhandling.CommandHandler
import org.axonframework.commandhandling.model.AggregateIdentifier
import org.axonframework.commandhandling.model.AggregateLifecycle
import org.axonframework.eventsourcing.EventSourcingHandler
import org.axonframework.spring.stereotype.Aggregate

@Aggregate
class DataEntryAggregate() {

  @AggregateIdentifier
  private lateinit var entryId: EntryId
  private lateinit var entryType: EntryType

  @CommandHandler
  constructor(command: CreateDataEntryCommand) : this() {
    AggregateLifecycle.apply(DataEntryCreatedEvent(
      entryId = command.entryId,
      entryType = command.entryType,
      payload = command.payload,
      correlations = command.correlations
    ))
  }

  @EventSourcingHandler
  fun on(event: DataEntryCreatedEvent) {
    this.entryId = event.entryId
    this.entryType = event.entryId
  }
}
