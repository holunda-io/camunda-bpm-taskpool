package io.holunda.camunda.taskpool.core

import io.holunda.camunda.taskpool.core.process.ProcessDefinitionAggregate
import io.holunda.camunda.taskpool.core.task.TaskAggregate
import org.axonframework.eventsourcing.EventSourcingRepository
import org.axonframework.eventsourcing.eventstore.EventStore
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
  fun processDefinitionAggregateRepository(eventStore: EventStore): EventSourcingRepository<ProcessDefinitionAggregate> {
    return EventSourcingRepository
      .builder(ProcessDefinitionAggregate::class.java)
      .eventStore(eventStore)
      .build()
  }

}
