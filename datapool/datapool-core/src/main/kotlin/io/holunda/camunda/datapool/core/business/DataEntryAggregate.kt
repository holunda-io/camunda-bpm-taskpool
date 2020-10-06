package io.holunda.camunda.datapool.core.business

import io.holunda.camunda.taskpool.api.business.*
import org.axonframework.commandhandling.CommandHandler
import org.axonframework.eventsourcing.EventSourcingHandler
import org.axonframework.modelling.command.AggregateIdentifier
import org.axonframework.modelling.command.AggregateLifecycle
import org.axonframework.spring.stereotype.Aggregate

/**
 * Aggregate representing a data entry.
 */
@Aggregate
class DataEntryAggregate() {

  @AggregateIdentifier
  private lateinit var dataIdentity: String

  /**
   * Handle creation of data entry aggregate.
   */
  @CommandHandler
  constructor(command: CreateDataEntryCommand) : this() {
    AggregateLifecycle.apply(DataEntryCreatedEvent(
      entryId = command.dataEntryChange.entryId,
      entryType = command.dataEntryChange.entryType,
      name = command.dataEntryChange.name,
      type = command.dataEntryChange.type,
      applicationName = command.dataEntryChange.applicationName,
      state = command.dataEntryChange.state,
      description = command.dataEntryChange.description,
      payload = command.dataEntryChange.payload,
      correlations = command.dataEntryChange.correlations,
      createModification = command.dataEntryChange.modification,
      authorizations = command.dataEntryChange.authorizationChanges,
      formKey = command.dataEntryChange.formKey
    ))
  }

  /**
   * Handle update command.
   */
  @CommandHandler
  fun handle(command: UpdateDataEntryCommand) {
    AggregateLifecycle.apply(DataEntryUpdatedEvent(
      entryId = command.dataEntryChange.entryId,
      entryType = command.dataEntryChange.entryType,
      name = command.dataEntryChange.name,
      type = command.dataEntryChange.type,
      applicationName = command.dataEntryChange.applicationName,
      state = command.dataEntryChange.state,
      description = command.dataEntryChange.description,
      payload = command.dataEntryChange.payload,
      correlations = command.dataEntryChange.correlations,
      updateModification = command.dataEntryChange.modification,
      authorizations = command.dataEntryChange.authorizationChanges,
      formKey = command.dataEntryChange.formKey
    ))
  }

  /**
   * React on event.
   */
  @EventSourcingHandler
  fun on(event: DataEntryCreatedEvent) {
    this.dataIdentity = dataIdentityString(entryType = event.entryType, entryId = event.entryId)
  }

}
