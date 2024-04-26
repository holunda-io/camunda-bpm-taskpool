package io.holunda.polyflow.taskpool.core.process

import io.holunda.camunda.taskpool.api.process.variable.ChangeProcessVariablesForExecutionCommand
import org.axonframework.commandhandling.CommandHandler
import org.axonframework.eventsourcing.EventSourcingRepository
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component

/**
 * This handler makes sure that an update of variables can be sent without a create of the process instance.
 */
@Component
class ProcessInstanceVariablesChangeHandler(
  @Lazy
  val eventSourcingRepository: EventSourcingRepository<ProcessInstanceAggregate>
) {

  /**
   * Update variables on existing instance aggregate or create a new one, if missed.
   */
  @CommandHandler
  fun update(command: ChangeProcessVariablesForExecutionCommand) {
    eventSourcingRepository.loadOrCreate(command.sourceReference.instanceId) { ProcessInstanceAggregate() }
      .execute { it.changeVariables(command) }
  }

}
