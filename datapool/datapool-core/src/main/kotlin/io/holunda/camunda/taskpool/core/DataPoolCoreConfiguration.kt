package io.holunda.camunda.taskpool.core

import io.holunda.camunda.taskpool.core.business.DataEntryAggregate
import org.axonframework.eventsourcing.EventSourcingRepository
import org.axonframework.eventsourcing.eventstore.EventStore
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration


@Configuration
@ComponentScan
open class DataPoolCoreConfiguration {

  @Bean
  open fun dataEntryAggregateRepository(eventStore: EventStore): EventSourcingRepository<DataEntryAggregate> {
    return EventSourcingRepository.builder(DataEntryAggregate::class.java).eventStore(eventStore).build()
  }

}
