package io.holunda.polyflow.taskpool.core

import io.holunda.polyflow.taskpool.core.process.ProcessDefinitionAggregate
import io.holunda.polyflow.taskpool.core.process.ProcessInstanceAggregate
import io.holunda.polyflow.taskpool.core.task.TaskAggregate
import org.axonframework.eventsourcing.EventSourcingRepository
import org.axonframework.eventsourcing.eventstore.EventStore
import org.axonframework.modelling.command.Aggregate
import org.axonframework.modelling.command.AggregateNotFoundException
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import java.util.*

/**
 * Main configuration of the task pool core.
 */
@Configuration
@ComponentScan
class TaskPoolCoreConfiguration {

  companion object {
    const val TASK_AGGREGATE_REPOSITORY = "taskAggregateRepository"
    const val PROCESS_DEFINITION_AGGREGATE_REPOSITORY = "processDefinitionAggregateRepository"
    const val PROCESS_INSTANCE_AGGREGATE_REPOSITORY = "processInstanceAggregateRepository"
  }

  /**
   * Provide repository for task aggregates.
   */
  @Bean(TASK_AGGREGATE_REPOSITORY)
  fun taskAggregateRepository(eventStore: EventStore): EventSourcingRepository<TaskAggregate> {
    return EventSourcingRepository
      .builder(TaskAggregate::class.java)
      .eventStore(eventStore)
      .build()
  }

  /**
   * Provide repository for process definition aggregates.
   */
  @Bean(PROCESS_DEFINITION_AGGREGATE_REPOSITORY)
  fun processDefinitionAggregateRepository(eventStore: EventStore): EventSourcingRepository<ProcessDefinitionAggregate> {
    return EventSourcingRepository
      .builder(ProcessDefinitionAggregate::class.java)
      .eventStore(eventStore)
      .build()
  }

  /**
   * Provide repository for process instance aggregates.
   */
  @Bean(PROCESS_INSTANCE_AGGREGATE_REPOSITORY)
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

/**
 * Extending optional to be able to react on presence and absence with kotlin callback functions.
 * @param presentConsumer consumer if present.
 * @param missingCallback callback if absend.
 */
fun <T> Optional<T>.ifPresentOrElse(presentConsumer: (T) -> Unit, missingCallback: () -> Unit) {
  if (this.isPresent) {
    presentConsumer(this.get())
  } else {
    missingCallback()
  }
}

