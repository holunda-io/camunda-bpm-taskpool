package io.holunda.polyflow.datapool.core

import io.holunda.polyflow.datapool.core.business.DataEntryAggregate
import org.axonframework.eventsourcing.EventSourcingRepository
import org.axonframework.eventsourcing.eventstore.EventStore
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration


/**
 * Configuration of polyflow data pool core.
 */
@Configuration
@ComponentScan
class DataPoolCoreConfiguration {

  /**
   * Provides an event sourcing repository for data entry aggregates.
   */
  @Bean
  fun dataEntryAggregateRepository(eventStore: EventStore): EventSourcingRepository<DataEntryAggregate> {
    return EventSourcingRepository.builder(DataEntryAggregate::class.java).eventStore(eventStore).build()
  }
}
