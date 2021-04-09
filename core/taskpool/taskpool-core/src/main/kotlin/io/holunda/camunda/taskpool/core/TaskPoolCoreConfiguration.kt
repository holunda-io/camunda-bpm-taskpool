package io.holunda.camunda.taskpool.core

import io.holunda.camunda.taskpool.core.process.ProcessDefinitionAggregate
import io.holunda.camunda.taskpool.core.process.ProcessInstanceAggregate
import io.holunda.camunda.taskpool.core.task.TaskAggregate
import org.axonframework.eventsourcing.EventSourcingRepository
import org.axonframework.eventsourcing.eventstore.EventStore
import org.axonframework.modelling.command.Aggregate
import org.axonframework.modelling.command.AggregateNotFoundException
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import java.util.*


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

  @Bean
  fun processInstanceAggregateRepository(eventStore: EventStore): EventSourcingRepository<ProcessInstanceAggregate> {
    return EventSourcingRepository
      .builder(ProcessInstanceAggregate::class.java)
      .eventStore(eventStore)
      .build()
  }

}

/**
 * Tries to load an aggregate for given identifier.
 * @param id aggregate identifier.
 * @return Optional result, if found.
 */
fun <T : Any> EventSourcingRepository<T>.loadOptional(id: String): Optional<Aggregate<T>> =
  try {
    Optional.of(this.load(id))
  } catch (e: AggregateNotFoundException) {
    Optional.empty()
  }

fun <T> Optional<T>.ifPresentOrElse(presentConsumer: (T) -> Unit, missingCallback: () -> Unit) {
  if (this.isPresent) {
    presentConsumer(this.get())
  } else {
    missingCallback()
  }
}

