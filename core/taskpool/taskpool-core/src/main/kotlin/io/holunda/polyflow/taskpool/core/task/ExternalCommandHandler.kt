package io.holunda.polyflow.taskpool.core.task

import io.holunda.camunda.taskpool.api.task.*
import io.holunda.polyflow.taskpool.core.ifPresentOrElse
import io.holunda.polyflow.taskpool.core.loadOptional
import org.axonframework.commandhandling.CommandHandler
import org.axonframework.commandhandling.GenericCommandMessage
import org.axonframework.eventsourcing.EventSourcingRepository
import org.axonframework.messaging.MetaData
import org.axonframework.modelling.command.Aggregate
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component

/**
 * Handler allowing to re-submit a create command for already existing task.
 */
@Component
class ExternalCommandHandler(
  @Lazy
  val eventSourcingRepository: EventSourcingRepository<TaskAggregate>
) {

  /**
   * Create a new aggregate (default) or load existing and replay the creation command if already there.
   */
  @CommandHandler
  fun create(command: CreateTaskCommand, metadata: MetaData) {
    eventSourcingRepository.loadOptional(command.id).ifPresentOrElse(
      presentConsumer = { aggregate -> aggregate.handle(GenericCommandMessage(command, metadata)) },
      missingCallback = { eventSourcingRepository.newInstance { TaskAggregate() }.handle(GenericCommandMessage(command, metadata)) }
    )
  }

  /**
   * Delivers a batch.
   * @param batch batch command.
   */
  @CommandHandler
  fun handleBatch(batch: BatchCommand, metadata: MetaData) {
    eventSourcingRepository.loadOptional(batch.id).ifPresentOrElse(
      presentConsumer = { aggregate ->
        aggregate
          .apply { batch.commands.forEach { command -> handle(GenericCommandMessage(command, metadata)) } }
      },
      missingCallback = {
        eventSourcingRepository.newInstance { TaskAggregate() }
          .apply { batch.commands.forEach { command -> handle(GenericCommandMessage(command, metadata)) } }
      }
    )
  }
}
