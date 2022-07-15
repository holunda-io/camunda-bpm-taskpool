package io.holunda.polyflow.datapool.core.business

import io.holunda.camunda.taskpool.api.business.CreateDataEntryCommand
import io.holunda.camunda.taskpool.api.business.CreateOrUpdateDataEntryCommand
import io.holunda.camunda.taskpool.api.business.UpdateDataEntryCommand
import io.holunda.polyflow.datapool.ifPresentOrElse
import mu.KLogging
import org.axonframework.commandhandling.CommandHandler
import org.axonframework.eventsourcing.EventSourcingRepository
import org.axonframework.messaging.MetaData
import org.axonframework.modelling.command.Aggregate
import org.axonframework.modelling.command.AggregateNotFoundException
import org.springframework.stereotype.Component
import java.util.*

/**
 * Handler taking care of existence of data entry aggregate.
 */
@Component
class CreateOrUpdateCommandHandler(
  private val eventSourcingRepository: EventSourcingRepository<DataEntryAggregate>
) {

  companion object: KLogging()
  /**
   * Receives create-or-update and decides what to do.
   * @param command command to create or update the aggregate.
   * @param metaData metadata of the message.
   */
  @CommandHandler
  fun createOrUpdate(command: CreateOrUpdateDataEntryCommand, metaData: MetaData) {
    logger.trace { "Processing createOrUpdate command for ${command.dataIdentity}" }
    loadAggregate(command.dataIdentity).ifPresentOrElse(
      presentConsumer = { aggregate ->
        val updateCommand = UpdateDataEntryCommand(
          dataEntryChange = command.dataEntryChange
        )
        logger.trace { "Aggregate found. Updating it passing command $updateCommand" }
        aggregate.invoke {
          it.handle(
            command = updateCommand
          )
        }
      },
      missingCallback = {
        val createCommand = CreateDataEntryCommand(
          dataEntryChange = command.dataEntryChange
        )
        logger.trace { "No aggregate found. Creating a new data entry aggregate and passing command $command" }
        eventSourcingRepository.newInstance {
          DataEntryAggregate(
            command = createCommand
          )
        }
      }
    )
  }

  /**
   * Loads an aggregate if such exists.
   */
  private fun loadAggregate(id: String): Optional<Aggregate<DataEntryAggregate>> =
    try {
      Optional.of(eventSourcingRepository.load(id))
    } catch (e: AggregateNotFoundException) {
      Optional.empty()
    }
}
