package io.holunda.polyflow.datapool.core.repository

import org.axonframework.eventhandling.DomainEventMessage
import org.axonframework.eventsourcing.AggregateDeletedException
import org.axonframework.eventsourcing.EventSourcedAggregate
import org.axonframework.eventsourcing.EventSourcingRepository
import org.axonframework.eventsourcing.SnapshotTriggerDefinition
import org.axonframework.eventsourcing.eventstore.DomainEventStream
import org.axonframework.eventsourcing.eventstore.EventStore
import org.axonframework.modelling.command.AggregateNotFoundException
import org.axonframework.modelling.command.RepositoryProvider
import java.util.function.Predicate
import java.util.stream.Stream

/**
 * Special repository for loading the aggregate which state is stored in the first event.
 */
open class FirstEventOnlyEventSourcingRepository<T>(
  private val internalBuilder: FirstEventOnlyEventSourcingRepositoryBuilder<T>
) : EventSourcingRepository<T>(internalBuilder) {

  companion object {
    /**
     * Repository builder.
     */
    @JvmStatic
    fun <T> builder(clazz: Class<T>): FirstEventOnlyEventSourcingRepositoryBuilder<T> = FirstEventOnlyEventSourcingRepositoryBuilder(clazz)
  }

  private val triggerDefinition: SnapshotTriggerDefinition = internalBuilder.internalSnapshotTriggerDefinition()
  private val eventStore: EventStore = internalBuilder.internalEventStore()
  private val repositoryProvider: RepositoryProvider? = internalBuilder.internalRepositoryProvider()
  private val eventStreamFilter: Predicate<in DomainEventMessage<*>>? = internalBuilder.internalEventStreamFilter()


  // the reason for this repository is NOT to initialize the state based on the entire stream of events
  // this method is a 1:1 copy of the super method, avoiding the state initialization and reading the
  // events, since the state of the Aggregate is its aggregate identifier only, which is already contained in the first event.
  override fun doLoadWithLock(aggregateIdentifier: String, expectedVersion: Long?): EventSourcedAggregate<T> {
    val trigger = triggerDefinition.prepareTrigger(aggregateFactory.aggregateType)
    val eventStream: DomainEventStream = readEvents(aggregateIdentifier)
    return if (!eventStream.hasNext()) {
      throw AggregateNotFoundException(aggregateIdentifier, "The aggregate was not found in the event store")
    } else {
      val aggregate: EventSourcedAggregate<T> = EventSourcedAggregate.initialize(
        aggregateFactory.createAggregateRoot(aggregateIdentifier, eventStream.peek()),
        aggregateModel(), eventStore, repositoryProvider, trigger
      )

      // we tweak the stream to have one message (first one), but still have the correct index
      val lastSequenceNumber = eventStore.lastSequenceNumberFor(aggregateIdentifier).orElse(0L)
      val firstEventStream = DomainEventStream.of(Stream.of(eventStream.peek())) { lastSequenceNumber }
      // still initialize the aggregate, but use only one event.
      aggregate.initializeState(firstEventStream)
      if (aggregate.isDeleted) {
        throw AggregateDeletedException(aggregateIdentifier)
      } else {
        aggregate
      }
    }
  }
}


