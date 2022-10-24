package io.holunda.polyflow.taskpool.core.process

import io.holunda.camunda.taskpool.api.process.definition.RegisterProcessDefinitionCommand
import io.holunda.polyflow.taskpool.core.loadOptional
import io.holunda.polyflow.taskpool.core.ifPresentOrElse
import org.axonframework.commandhandling.CommandHandler
import org.axonframework.eventsourcing.EventSourcingRepository
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component

/**
 * Handler allowing to register existing definition multiple times.
 */
@Component
class ProcessDefinitionRegisterCommandHandler(
  @Lazy
  val eventSourcingRepository: EventSourcingRepository<ProcessDefinitionAggregate>
) {

  /**
   * Submits a register definition command to an existing aggregate or a new aggregate.
   */
  @CommandHandler
  fun register(command: RegisterProcessDefinitionCommand) {
    eventSourcingRepository.loadOptional(command.processDefinitionId).ifPresentOrElse(
      presentConsumer = { aggregate ->
        // re-apply creation.
        aggregate.invoke {
          it.handle(command)
        }
      },
      missingCallback = {
        eventSourcingRepository.newInstance {
          ProcessDefinitionAggregate()
            .apply {
              handle(command)
            }
        }
      }
    )
  }
}
