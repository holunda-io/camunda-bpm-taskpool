package io.holunda.camunda.taskpool.core.process

import io.holunda.camunda.taskpool.api.process.definition.RegisterProcessDefinitionCommand
import io.holunda.camunda.taskpool.api.task.CreateTaskCommand
import io.holunda.camunda.taskpool.core.task.TaskAggregate
import io.holunda.camunda.taskpool.core.task.ifPresentOrElse
import org.axonframework.commandhandling.CommandHandler
import org.axonframework.eventsourcing.EventSourcingRepository
import org.axonframework.modelling.command.Aggregate
import org.axonframework.modelling.command.AggregateNotFoundException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.util.*

@Component
class CreateProcessDefinitionCommandHandler {

  @Autowired
  private lateinit var eventSourcingRepository: EventSourcingRepository<ProcessDefinitionAggregate>

  @CommandHandler
  fun create(command: RegisterProcessDefinitionCommand) {
    loadAggregate(command.processDefinitionId).ifPresentOrElse(
      presentConsumer = { aggregate ->
        // re-apply creation.
        aggregate.invoke {
          it.handle(command)
        }
      },
      missingCallback = {
        eventSourcingRepository.newInstance {
          ProcessDefinitionAggregate().apply {
            handle(command)
          }
        }
      }
    )
  }

  private fun loadAggregate(id: String): Optional<Aggregate<ProcessDefinitionAggregate>> =
    try {
      Optional.of(eventSourcingRepository.load(id))
    } catch (e: AggregateNotFoundException) {
      Optional.empty()
    }
}
