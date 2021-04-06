package io.holunda.camunda.taskpool.core.process

import io.holunda.camunda.taskpool.api.process.variable.UpdateProcessVariableCommand
import io.holunda.camunda.taskpool.core.ifPresentOrElse
import io.holunda.camunda.taskpool.core.loadOptional
import org.axonframework.commandhandling.CommandHandler
import org.axonframework.eventsourcing.EventSourcingRepository
import org.springframework.stereotype.Component

/**
 * This handler makes sure that an update can be send without a create.
 */
@Component
class ProcessVariableUpdateHandler(
  val eventSourcingRepository: EventSourcingRepository<ProcessVariableAggregate>
) {

  @CommandHandler
  fun update(command: UpdateProcessVariableCommand) {
    eventSourcingRepository.loadOptional(command.variableInstanceId).ifPresentOrElse(
      presentConsumer = { aggregate ->
        // re-apply creation.
        aggregate.invoke {
          it.apply {
            update(command)
          }
        }
      },
      missingCallback = {
        eventSourcingRepository.newInstance {
          ProcessVariableAggregate().apply {
            update(command)
          }
        }
      }
    )
  }

}
