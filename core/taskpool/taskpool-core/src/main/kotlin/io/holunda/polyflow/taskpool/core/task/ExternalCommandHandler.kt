package io.holunda.polyflow.taskpool.core.task

import io.holunda.camunda.taskpool.api.task.CreateTaskCommand
import org.axonframework.commandhandling.CommandHandler
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
    eventSourcingRepository.loadOrCreate(command.id) { TaskAggregate() }
      .execute { it.handle(command, metadata) }
  }
}
