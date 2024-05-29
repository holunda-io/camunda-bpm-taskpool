package io.holunda.polyflow.datapool.core

import com.fasterxml.jackson.databind.ObjectMapper
import io.holunda.polyflow.bus.jackson.JsonAutoDetectAnyVisibility
import io.holunda.polyflow.datapool.core.business.CreateOrUpdateCommandHandler
import io.holunda.polyflow.datapool.core.business.DataEntryAggregate
import io.holunda.polyflow.datapool.core.business.upcaster.DataEntryCreatedEventUpcaster
import io.holunda.polyflow.datapool.core.repository.FirstEventOnlyEventSourcingRepository
import mu.KLogging
import org.axonframework.common.caching.Cache
import org.axonframework.common.caching.WeakReferenceCache
import org.axonframework.eventsourcing.EventCountSnapshotTriggerDefinition
import org.axonframework.eventsourcing.EventSourcingRepository
import org.axonframework.eventsourcing.SnapshotTriggerDefinition
import org.axonframework.eventsourcing.Snapshotter
import org.axonframework.eventsourcing.eventstore.EventStore
import org.axonframework.messaging.annotation.ParameterResolverFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import


/**
 * Configuration of polyflow data pool core axon setup.
 */
@Configuration
@Import(
  DataEntryAggregate::class,
  CreateOrUpdateCommandHandler::class,
  DataEntryCreatedEventUpcaster::class
)
class DataPoolCoreAxonConfiguration {
  companion object : KLogging() {
    const val DATA_ENTRY_REPOSITORY = "dataEntryEventSourcingRepository"
    const val DATA_ENTRY_SNAPSHOTTER = "dataEntrySnapshotter"
    const val DATA_ENTRY_CACHE = "dataEntryCache"
  }

  /**
   * Provides an event sourcing repository for data entry aggregates that creates the aggregate and uses the first event in the stream only.
   * This repository avoids loading of all events and will avoid usage of snapshots.
   */
  @ConditionalOnProperty(
    name = ["polyflow.core.data-entry.event-sourcing-repository-type"],
    havingValue = "io.holunda.polyflow.datapool.core.repository.FirstEventOnlyEventSourcingRepository"
  )
  @Bean(DATA_ENTRY_REPOSITORY)
  fun firstEventDataEntryAggregateRepository(
    eventStore: EventStore,
    factory: ParameterResolverFactory,
    @Qualifier(DATA_ENTRY_CACHE) dataEntryCache: Cache,
    @Qualifier(DATA_ENTRY_SNAPSHOTTER) dataEntryAggregateSnapshotterTriggerDefinition: SnapshotTriggerDefinition
  ): EventSourcingRepository<DataEntryAggregate> {
    return FirstEventOnlyEventSourcingRepository
      .builder(DataEntryAggregate::class.java)
      .parameterResolverFactory(factory)
      .cache(dataEntryCache)
      .snapshotTriggerDefinition(dataEntryAggregateSnapshotterTriggerDefinition)
      .eventStore(eventStore)
      .build()
  }

  /**
   * Provides a standard event sourcing repository for data entry aggregate.
   */
  @ConditionalOnProperty(
    name = ["polyflow.core.data-entry.event-sourcing-repository-type"],
    havingValue = "org.axonframework.eventsourcing.EventSourcingRepository",
    matchIfMissing = true
  )
  @Bean(DATA_ENTRY_REPOSITORY)
  fun dataEntryAggregateRepository(
    eventStore: EventStore,
    factory: ParameterResolverFactory,
    @Qualifier(DATA_ENTRY_CACHE) dataEntryCache: Cache,
    @Qualifier(DATA_ENTRY_SNAPSHOTTER) dataEntryAggregateSnapshotterTriggerDefinition: SnapshotTriggerDefinition
  ): EventSourcingRepository<DataEntryAggregate> {
    return EventSourcingRepository
      .builder(DataEntryAggregate::class.java)
      .parameterResolverFactory(factory)
      .cache(dataEntryCache)
      .snapshotTriggerDefinition(dataEntryAggregateSnapshotterTriggerDefinition)
      .eventStore(eventStore)
      .build()
  }

  /**
   * Register snapshotter. It will trigger if the aggregate is loaded by the series of events and the series is exceeding the snapshot.
   * This is the case by the standard [EventSourcingRepository] is used, but will never happen if the [FirstEventOnlyEventSourcingRepository] is used.
   */
  @Bean(DATA_ENTRY_SNAPSHOTTER)
  fun dataEntryAggregateSnapshotterTriggerDefinition(snapshotter: Snapshotter, dataPoolProperties: DataPoolProperties): SnapshotTriggerDefinition {
    return EventCountSnapshotTriggerDefinition(snapshotter, dataPoolProperties.snapshotThreshold)
  }

  /**
   * Use weak reference cache.
   */
  @Bean(DATA_ENTRY_CACHE)
  fun dataEntryCache(): Cache = WeakReferenceCache()

  @Autowired
  fun configureJackson(objectMapper: ObjectMapper) {
    objectMapper.configurePolyflowJacksonObjectMapperForDatapool()
  }
}

fun ObjectMapper.configurePolyflowJacksonObjectMapperForDatapool(): ObjectMapper {
  addMixIn(DataEntryAggregate::class.java, JsonAutoDetectAnyVisibility::class.java)
  return this
}

