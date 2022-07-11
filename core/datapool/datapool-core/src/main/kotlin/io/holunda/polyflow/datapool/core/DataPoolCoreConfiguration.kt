package io.holunda.polyflow.datapool.core

import io.holunda.polyflow.datapool.core.business.DataEntryAggregate
import org.axonframework.eventsourcing.EventCountSnapshotTriggerDefinition
import org.axonframework.eventsourcing.EventSourcingRepository
import org.axonframework.eventsourcing.SnapshotTriggerDefinition
import org.axonframework.eventsourcing.Snapshotter
import org.axonframework.eventsourcing.eventstore.EventStore
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration


/**
 * Configuration of polyflow data pool core.
 */
@Configuration
@ComponentScan
@EnableConfigurationProperties(
  DataPoolProperties::class
)
class DataPoolCoreConfiguration {

  companion object {
    const val SNAPSHOT_TRIGGER_DEFINITION = "dataEntrySnapshotTrigger"
  }

  /**
   * Provides an event sourcing repository for data entry aggregates.
   */
  @Bean
  fun dataEntryAggregateRepository(eventStore: EventStore): EventSourcingRepository<DataEntryAggregate> {
    return EventSourcingRepository.builder(DataEntryAggregate::class.java).eventStore(eventStore).build()
  }

  @Bean(name = [SNAPSHOT_TRIGGER_DEFINITION])
  open fun dataEntrySnapshotTrigger(snapshotter: Snapshotter): SnapshotTriggerDefinition {
    return EventCountSnapshotTriggerDefinition(snapshotter, 1)
  }

}
