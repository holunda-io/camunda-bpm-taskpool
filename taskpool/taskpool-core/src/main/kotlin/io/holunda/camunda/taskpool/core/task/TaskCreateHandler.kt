package io.holunda.camunda.taskpool.core.task

import io.holunda.camunda.taskpool.api.task.CreateTaskCommand
import org.axonframework.commandhandling.CommandHandler
import org.axonframework.eventsourcing.EventSourcingRepository
import org.axonframework.modelling.command.Aggregate
import org.axonframework.modelling.command.AggregateNotFoundException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.util.*

@Component
class CreateTaskCommandHandler {

  @Autowired
  private lateinit var eventSourcingRepository: EventSourcingRepository<TaskAggregate>

  @CommandHandler
  fun create(command: CreateTaskCommand) {
    loadAggregate(command.id).ifPresentOrElse(
      presentConsumer = { aggregate ->
        // re-apply creation.
        aggregate.invoke {
          it.handle(command)
        }
      },
      missingCallback = {
        eventSourcingRepository.newInstance {
          TaskAggregate().apply {
            handle(command)
          }
        }
      }
    )
  }

  private fun loadAggregate(id: String): Optional<Aggregate<TaskAggregate>> =
    try {
      Optional.of(eventSourcingRepository.load(id))
    } catch (e: AggregateNotFoundException) {
      Optional.empty()
    }
}

fun <T> Optional<T>.ifPresentOrElse(presentConsumer: (T) -> Unit, missingCallback: () -> Unit) {
  if (this.isPresent) {
    presentConsumer(this.get())
  } else {
    missingCallback()
  }
}

