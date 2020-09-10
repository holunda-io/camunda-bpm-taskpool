package io.holunda.camunda.taskpool.core

import com.fasterxml.jackson.databind.ObjectMapper
import io.holunda.camunda.taskpool.core.task.TaskAggregate
import io.holunda.camunda.taskpool.upcast.definition.ProcessDefinitionEventNullTo1Upcaster
import org.axonframework.eventsourcing.EventSourcingRepository
import org.axonframework.eventsourcing.eventstore.EventStore
import org.axonframework.serialization.upcasting.event.EventUpcaster
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration


@Configuration
@ComponentScan
class TaskPoolCoreConfiguration {

  @Bean
  fun taskAggregateRepository(eventStore: EventStore): EventSourcingRepository<TaskAggregate> {
    return EventSourcingRepository
      .builder(TaskAggregate::class.java)
      .eventStore(eventStore)
      .build()
  }


  @Bean
  fun processDefinitionEventUpcaster(objectMapper: ObjectMapper): EventUpcaster =
    ProcessDefinitionEventNullTo1Upcaster(objectMapper)
}
