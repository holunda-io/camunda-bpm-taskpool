package io.holunda.polyflow.datapool.core.business

import io.holunda.camunda.taskpool.api.business.*
import io.holunda.polyflow.datapool.core.DataPoolCoreConfiguration
import mu.KLogging
import org.axonframework.commandhandling.CommandHandler
import org.axonframework.eventsourcing.EventSourcingHandler
import org.axonframework.modelling.command.AggregateIdentifier
import org.axonframework.modelling.command.AggregateLifecycle
import org.axonframework.spring.stereotype.Aggregate

/**
 * Aggregate representing a data entry.
 * Currently, it has no state.
 * The aggregate is manually created by the CreateOrUpdateCommandHandler.
 */
@Aggregate(
  repository = DataPoolCoreConfiguration.DATA_ENTRY_REPOSITORY,
  snapshotTriggerDefinition = DataPoolCoreConfiguration.DATA_ENTRY_SNAPSHOTTER,
  cache = DataPoolCoreConfiguration.DATA_ENTRY_CACHE,
)
class DataEntryAggregate() {

  companion object: KLogging()

  @AggregateIdentifier
  private lateinit var dataIdentity: String

  /**
   * Handle creation of data entry aggregate.
   */
  @CommandHandler
  constructor(command: CreateDataEntryCommand) : this() {
    AggregateLifecycle.apply(
      command.createdEvent()
    )
  }

  /**
   * Handle update command.
   */
  @CommandHandler
  fun handle(command: UpdateDataEntryCommand) {
    AggregateLifecycle.apply(
      command.updatedEvent()
    )
  }

  /**
   * React on create event.
   */
  @EventSourcingHandler
  fun on(event: DataEntryCreatedEvent) {
    val identity = dataIdentityString(entryType = event.entryType, entryId = event.entryId)
    if (logger.isDebugEnabled) {
      logger.debug { "Created $identity." }
    }
    if (logger.isTraceEnabled) {
      logger.trace { "Created $identity with: $event" }
    }
    this.dataIdentity = identity
  }

  /**
   * React on update event.
   */
  @EventSourcingHandler
  fun on(event: DataEntryUpdatedEvent) {
    if (logger.isDebugEnabled) {
      logger.debug { "Updated $dataIdentity." }
    }
    if (logger.isTraceEnabled) {
      logger.trace { "Updated $dataIdentity with: $event" }
    }
  }
}
