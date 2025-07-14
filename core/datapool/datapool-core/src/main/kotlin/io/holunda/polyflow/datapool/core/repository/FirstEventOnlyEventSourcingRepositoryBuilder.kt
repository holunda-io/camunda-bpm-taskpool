package io.holunda.polyflow.datapool.core.repository

import org.axonframework.eventhandling.DomainEventMessage
import org.axonframework.eventsourcing.EventSourcingRepository
import org.axonframework.eventsourcing.SnapshotTriggerDefinition
import org.axonframework.eventsourcing.eventstore.EventStore
import org.axonframework.modelling.command.RepositoryProvider
import java.util.function.Predicate

/**
 * Builder for the factory.
 */
class FirstEventOnlyEventSourcingRepositoryBuilder<T>(clazz: Class<T>) : EventSourcingRepository.Builder<T>(clazz) {

  /**
   * Internal snapshotter trigger definition exposed to the users.
   */
  fun internalSnapshotTriggerDefinition(): SnapshotTriggerDefinition = super.snapshotTriggerDefinition

  /**
   * Internal event store exposed to the users.
   */
  fun internalEventStore(): EventStore = super.eventStore

  /**
   * Internal repository provider exposed to the users.
   */
  fun internalRepositoryProvider(): RepositoryProvider? = super.repositoryProvider

  /**
   * Internal event stream filter exposed to the user.
   */
  fun internalEventStreamFilter(): Predicate<in DomainEventMessage<*>>? = super.eventStreamFilter

  override fun eventStore(eventStore: EventStore?): FirstEventOnlyEventSourcingRepositoryBuilder<T> {
    super.eventStore(eventStore)
    return this
  }

  @Suppress("UNCHECKED_CAST")
  override fun <R : EventSourcingRepository<T>?> build(): R {
    return FirstEventOnlyEventSourcingRepository(this) as R
  }
}