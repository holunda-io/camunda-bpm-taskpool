package io.holunda.camunda.taskpool.core.task

import io.holunda.camunda.taskpool.api.task.CreateTaskCommand
import io.holunda.camunda.taskpool.core.ifPresentOrElse
import io.holunda.camunda.taskpool.core.loadOptional
import org.axonframework.commandhandling.CommandHandler
import org.axonframework.eventsourcing.EventSourcingRepository
import org.springframework.stereotype.Component

@Component
class CreateTaskCommandHandler(
  val eventSourcingRepository: EventSourcingRepository<TaskAggregate>
) {

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
