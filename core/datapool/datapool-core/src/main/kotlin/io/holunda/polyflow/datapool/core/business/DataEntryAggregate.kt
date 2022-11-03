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

  companion object : KLogging()

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
   * Handle update.
   */
  @CommandHandler
  fun handle(command: UpdateDataEntryCommand) {
    AggregateLifecycle.apply(
      command.updatedEvent()
    )
  }

  /**
   * Handle delete.
   */
  @CommandHandler
  fun handle(command: DeleteDataEntryCommand) {
    AggregateLifecycle.apply(
      command.deletedEvent()
    )
  }

  /**
   * React on created event.
   */
  @EventSourcingHandler
  fun on(event: DataEntryCreatedEvent) {
    this.dataIdentity = dataIdentityString(entryType = event.entryType, entryId = event.entryId)
    if (logger.isDebugEnabled) {
      logger.debug { "Created $dataIdentity." }
    }
    if (logger.isTraceEnabled) {
      logger.trace { "Created $dataIdentity with: $event" }
    }
  }

  /**
   * React on updated event.
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

  /**
   * React on deleted event.
   */
  @EventSourcingHandler
  fun on(event: DataEntryDeletedEvent) {
    if (logger.isDebugEnabled) {
      logger.debug { "Deleted $dataIdentity." }
    }
    if (logger.isTraceEnabled) {
      logger.trace { "Deleted $dataIdentity with: $event" }
    }
    AggregateLifecycle.markDeleted()
  }
}
