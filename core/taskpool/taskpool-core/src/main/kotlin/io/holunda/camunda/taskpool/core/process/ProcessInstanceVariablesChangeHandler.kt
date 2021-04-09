package io.holunda.camunda.taskpool.core.process

import io.holunda.camunda.taskpool.api.process.variable.ChangeProcessVariablesForExecutionCommand
import io.holunda.camunda.taskpool.core.ifPresentOrElse
import io.holunda.camunda.taskpool.core.loadOptional
import org.axonframework.commandhandling.CommandHandler
import org.axonframework.eventsourcing.EventSourcingRepository
import org.springframework.stereotype.Component

/**
 * This handler makes sure that an update of variables can be send without a create of the process instance..
 */
@Component
class ProcessInstanceVariablesChangeHandler(
  val eventSourcingRepository: EventSourcingRepository<ProcessInstanceAggregate>
) {

  @CommandHandler
  fun update(command: ChangeProcessVariablesForExecutionCommand) {
    eventSourcingRepository.loadOptional(command.sourceReference.instanceId).ifPresentOrElse(
      presentConsumer = { aggregate ->
        // re-apply creation.
        aggregate.invoke {
          it.apply {
            changeVariables(command)
          }
        }
      },
      missingCallback = {
        eventSourcingRepository.newInstance {
          ProcessInstanceAggregate().apply {
            changeVariables(command)
          }
        }
      }
    )
  }

}
