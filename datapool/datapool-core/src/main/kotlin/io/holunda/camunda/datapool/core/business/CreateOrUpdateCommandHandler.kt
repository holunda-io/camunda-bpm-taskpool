package io.holunda.camunda.datapool.core.business

import io.holunda.camunda.datapool.ifPresentOrElse
import io.holunda.camunda.taskpool.api.business.CreateDataEntryCommand
import io.holunda.camunda.taskpool.api.business.CreateOrUpdateDataEntryCommand
import io.holunda.camunda.taskpool.api.business.UpdateDataEntryCommand
import org.axonframework.commandhandling.CommandHandler
import org.axonframework.eventsourcing.EventSourcingRepository
import org.axonframework.modelling.command.Aggregate
import org.axonframework.modelling.command.AggregateNotFoundException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.util.*

/**
 * Handler taking care of existence of data entry aggregate.
 */
@Component
class CreateOrUpdateCommandHandler {

  @Autowired
  private lateinit var eventSourcingRepository: EventSourcingRepository<DataEntryAggregate>

  /**
   * Receives create-or-update and decides what to do.
   */
  @CommandHandler
  fun createOrUpdate(command: CreateOrUpdateDataEntryCommand) {

    loadAggregate(command.dataIdentity).ifPresentOrElse(
      presentConsumer = { aggregate ->
        aggregate.invoke {
          it.handle(
            UpdateDataEntryCommand(
              dataEntry = command.dataEntry
            )
          )
        }
      },
      missingCallback = {
        eventSourcingRepository.newInstance {
          DataEntryAggregate(
            CreateDataEntryCommand(
              dataEntry = command.dataEntry
            )
          )
        }
      }
    )
  }

  private fun loadAggregate(id: String): Optional<Aggregate<DataEntryAggregate>> =
    try {
      Optional.of(eventSourcingRepository.load(id))
    } catch (e: AggregateNotFoundException) {
      Optional.empty()
    }
}
