package io.holunda.polyflow.taskpool.core.task

import io.holunda.camunda.taskpool.api.task.CreateTaskCommand
import io.holunda.polyflow.taskpool.core.ifPresentOrElse
import io.holunda.polyflow.taskpool.core.loadOptional
import org.axonframework.commandhandling.CommandHandler
import org.axonframework.commandhandling.GenericCommandMessage
import org.axonframework.eventsourcing.EventSourcingRepository
import org.axonframework.messaging.MetaData
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
}
