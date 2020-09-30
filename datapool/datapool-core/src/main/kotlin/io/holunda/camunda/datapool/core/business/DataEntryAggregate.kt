package io.holunda.camunda.datapool.core.business

import io.holunda.camunda.taskpool.api.business.*
import org.axonframework.commandhandling.CommandHandler
import org.axonframework.eventsourcing.EventSourcingHandler
import org.axonframework.messaging.MetaData
import org.axonframework.messaging.annotation.MetaDataValue
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
  constructor(command: CreateDataEntryCommand, metaData: MetaData) : this() {
    AggregateLifecycle.apply(DataEntryCreatedEvent(
      entryId = command.dataEntry.entryId,
      entryType = command.dataEntry.entryType,
      name = command.dataEntry.name,
      type = command.dataEntry.type,
      applicationName = command.dataEntry.applicationName,
      state = command.dataEntry.state,
      description = command.dataEntry.description,
      payload = command.dataEntry.payload,
      correlations = command.dataEntry.correlations,
      createModification = command.dataEntry.modification,
      authorizations = command.dataEntry.authorizations,
      formKey = command.dataEntry.formKey
    ), metaData)
  }

  /**
   * Handle update command.
   */
  @CommandHandler
  fun handle(command: UpdateDataEntryCommand, metaData: MetaData) {
    AggregateLifecycle.apply(DataEntryUpdatedEvent(
      entryId = command.dataEntry.entryId,
      entryType = command.dataEntry.entryType,
      name = command.dataEntry.name,
      type = command.dataEntry.type,
      applicationName = command.dataEntry.applicationName,
      state = command.dataEntry.state,
      description = command.dataEntry.description,
      payload = command.dataEntry.payload,
      correlations = command.dataEntry.correlations,
      updateModification = command.dataEntry.modification,
      authorizations = command.dataEntry.authorizations,
      formKey = command.dataEntry.formKey
    ), metaData)
  }

  /**
   * React on event.
   */
  @EventSourcingHandler
  fun on(event: DataEntryCreatedEvent) {
    this.dataIdentity = dataIdentityString(entryType = event.entryType, entryId = event.entryId)
  }

}
