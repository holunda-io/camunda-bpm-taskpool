package io.holunda.polyflow.taskpool.core.task

import io.holunda.camunda.taskpool.api.task.CreateTaskCommand
import io.holunda.camunda.taskpool.api.task.EngineTaskCommand
import io.holunda.camunda.taskpool.api.task.EngineTaskCommandFilter
import io.holunda.polyflow.taskpool.core.loadOptional
import org.axonframework.eventsourcing.EventSourcingRepository
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

/**
 * Filter checking the tasks in the event store.
 */
@Component
@ConditionalOnProperty(
  prefix = "polyflow.integration.collector.camunda.task.importer",
  name = ["task-filter-type"],
  havingValue = "eventstore",
  matchIfMissing = false
)
class TaskAggregateEngineTaskCommandFilter(
  private val eventSourcingRepository: EventSourcingRepository<TaskAggregate>
) : EngineTaskCommandFilter {

  override fun test(engineTaskCommand: EngineTaskCommand): Boolean {
    return when (engineTaskCommand) {

      is CreateTaskCommand -> {
        eventSourcingRepository.loadOptional(engineTaskCommand.id)
          .map { false } // if the task exists, the CreateCommand should not be emitted
          .orElse(true) // if the task doesn't exist, emit the CreateTaskCommand
      }

      else -> false // reject all others
    }
  }
}
