package io.holunda.polyflow.taskpool.core.task

import io.holunda.camunda.taskpool.api.task.CreateTaskCommand
import io.holunda.polyflow.taskpool.core.ifPresentOrElse
import io.holunda.polyflow.taskpool.core.loadOptional
import org.axonframework.commandhandling.CommandHandler
import org.axonframework.eventsourcing.EventSourcingRepository
import org.springframework.stereotype.Component
import org.springframework.context.annotation.Lazy

/**
 * Handler allowing to re-submit a create command for already existing task.
 */
@Component
class TaskCreateCommandHandler(
  @Lazy
  val eventSourcingRepository: EventSourcingRepository<TaskAggregate>
) {

  /**
   * Create a new aggregate (default) or load existing and replay the creation command if already there.
   */
  @CommandHandler
  fun create(command: CreateTaskCommand) {
    eventSourcingRepository.loadOptional(command.id).ifPresentOrElse(
      presentConsumer = { aggregate ->
        // re-apply creation.
        aggregate.invoke {
          it.handle(command)
        }
      },
      missingCallback = {
        eventSourcingRepository.newInstance {
          TaskAggregate().apply {
            handle(command)
          }
        }
      }
    )
  }
}
