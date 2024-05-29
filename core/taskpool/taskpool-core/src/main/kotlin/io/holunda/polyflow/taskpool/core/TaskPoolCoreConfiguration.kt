package io.holunda.polyflow.taskpool.core

import com.fasterxml.jackson.databind.ObjectMapper
import io.holunda.polyflow.bus.jackson.JsonAutoDetectAnyVisibility
import io.holunda.polyflow.taskpool.core.process.ProcessDefinitionAggregate
import io.holunda.polyflow.taskpool.core.process.ProcessInstanceAggregate
import io.holunda.polyflow.taskpool.core.task.TaskAggregate
import org.axonframework.common.caching.Cache
import org.axonframework.common.caching.WeakReferenceCache
import org.axonframework.eventsourcing.EventSourcingRepository
import org.axonframework.eventsourcing.eventstore.EventStore
import org.axonframework.messaging.annotation.ParameterResolverFactory
import org.axonframework.modelling.command.Aggregate
import org.axonframework.modelling.command.AggregateNotFoundException
import org.springframework.beans.factory.annotation.Autowired
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
    const val TASK_CACHE = "taskCache"
    const val PROCESS_DEFINITION_AGGREGATE_REPOSITORY = "processDefinitionAggregateRepository"
    const val PROCESS_INSTANCE_AGGREGATE_REPOSITORY = "processInstanceAggregateRepository"
  }

  /**
   * Provide repository for task aggregates.
   */
  @Bean(TASK_AGGREGATE_REPOSITORY)
  fun taskAggregateRepository(eventStore: EventStore, parameterResolverFactory: ParameterResolverFactory): EventSourcingRepository<TaskAggregate> {
    return EventSourcingRepository
      .builder(TaskAggregate::class.java)
      .parameterResolverFactory(parameterResolverFactory)
      .eventStore(eventStore)
      .build()
  }

  /**
   * Provide repository for process definition aggregates.
   */
  @Bean(PROCESS_DEFINITION_AGGREGATE_REPOSITORY)
  fun processDefinitionAggregateRepository(eventStore: EventStore, parameterResolverFactory: ParameterResolverFactory): EventSourcingRepository<ProcessDefinitionAggregate> {
    return EventSourcingRepository
      .builder(ProcessDefinitionAggregate::class.java)
      .parameterResolverFactory(parameterResolverFactory)
      .eventStore(eventStore)
      .build()
  }

  /**
   * Provide repository for process instance aggregates.
   */
  @Bean(PROCESS_INSTANCE_AGGREGATE_REPOSITORY)
  fun processInstanceAggregateRepository(eventStore: EventStore, parameterResolverFactory: ParameterResolverFactory): EventSourcingRepository<ProcessInstanceAggregate> {
    return EventSourcingRepository
      .builder(ProcessInstanceAggregate::class.java)
      .parameterResolverFactory(parameterResolverFactory)
      .eventStore(eventStore)
      .build()
  }

  @Autowired
  fun configureJackson(objectMapper: ObjectMapper) {
    objectMapper.configurePolyflowJacksonObjectMapperForTaskPool()
  }

  /**
   * Use weak reference cache.
   */
  @Bean(TASK_CACHE)
  fun taskCache(): Cache = WeakReferenceCache()
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
 * @param missingCallback callback if absent.
 */
fun <T> Optional<T>.ifPresentOrElse(presentConsumer: (T) -> Unit, missingCallback: () -> Unit) {
  if (this.isPresent) {
    presentConsumer(this.get())
  } else {
    missingCallback()
  }
}

fun ObjectMapper.configurePolyflowJacksonObjectMapperForTaskPool(): ObjectMapper {
  addMixIn(TaskAggregate::class.java, JsonAutoDetectAnyVisibility::class.java)
  return this
}

