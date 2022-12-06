package io.holunda.polyflow.taskpool.core.task

import io.holunda.camunda.taskpool.api.task.*
import io.holunda.polyflow.taskpool.core.ifPresentOrElse
import io.holunda.polyflow.taskpool.core.loadOptional
import org.axonframework.commandhandling.CommandHandler
import org.axonframework.eventsourcing.EventSourcingRepository
import org.springframework.stereotype.Component
import org.springframework.context.annotation.Lazy
import java.lang.IllegalArgumentException

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
    deliverCommand(command)
  }

  @CommandHandler
  fun handleBatch(command: BatchCommand) {

  }

  fun deliverCommand(command: EngineTaskCommand) {

    eventSourcingRepository.loadOptional(command.id).ifPresentOrElse(
      presentConsumer = { aggregate ->
        // re-apply creation.
        aggregate.invoke {
          when (command) {
            is CreateTaskCommand -> it.handle(command)
            is UpdateAttributeTaskCommand -> it.handle(command)
            is DeleteTaskCommand -> it.handle(command)
            is AssignTaskCommand -> it.handle(command)
            is AddCandidateGroupsCommand -> it.handle(command)
            is DeleteCandidateGroupsCommand -> it.handle(command)
            is AddCandidateUsersCommand -> it.handle(command)
            is DeleteCandidateUsersCommand -> it.handle(command)
            is CompleteTaskCommand -> it.handle(command)

            else -> throw IllegalArgumentException("Task aggregate for task ${command.id} can't handle command: ${command.eventName}")
          }
        }
      },
      missingCallback = {
        eventSourcingRepository.newInstance {
          TaskAggregate().apply {
            when (command) {
              is CreateTaskCommand -> handle(command)
              is UpdateAttributeTaskCommand -> handle(command)
              else -> throw IllegalArgumentException("Task aggregate for task ${command.id} doesn't exist, create command should be sent before sending this command: ${command.eventName}")
            }
          }
        }
      }
    )
  }
}
