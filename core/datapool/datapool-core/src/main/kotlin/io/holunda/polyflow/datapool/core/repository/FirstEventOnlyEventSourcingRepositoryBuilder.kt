package io.holunda.polyflow.datapool.core.repository

import org.axonframework.eventhandling.DomainEventMessage
import org.axonframework.eventsourcing.CachingEventSourcingRepository
import org.axonframework.eventsourcing.EventSourcingRepository
import org.axonframework.eventsourcing.SnapshotTriggerDefinition
import org.axonframework.eventsourcing.eventstore.EventStore
import org.axonframework.modelling.command.RepositoryProvider
import java.util.function.Predicate

/**
 * Builder for the factory.
 */
class FirstEventOnlyEventSourcingRepositoryBuilder<T>(clazz: Class<T>) : EventSourcingRepository.Builder<T>(clazz) {

  fun internalSnapshotTriggerDefinition(): SnapshotTriggerDefinition = super.snapshotTriggerDefinition
  fun internalEventStore(): EventStore = super.eventStore
  fun internalRepositoryProvider(): RepositoryProvider? = super.repositoryProvider
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