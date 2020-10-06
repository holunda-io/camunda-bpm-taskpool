package io.holunda.camunda.taskpool.core.process

import io.holunda.camunda.taskpool.api.process.definition.RegisterProcessDefinitionCommand
import io.holunda.camunda.taskpool.core.loadOptional
import io.holunda.camunda.taskpool.core.task.ifPresentOrElse
import org.axonframework.commandhandling.CommandHandler
import org.axonframework.eventsourcing.EventSourcingRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class CreateProcessDefinitionCommandHandler {

  @Autowired
  private lateinit var eventSourcingRepository: EventSourcingRepository<ProcessDefinitionAggregate>

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
