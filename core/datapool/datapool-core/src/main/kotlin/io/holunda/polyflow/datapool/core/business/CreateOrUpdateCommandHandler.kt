package io.holunda.polyflow.datapool.core.business

import io.github.oshai.kotlinlogging.KotlinLogging
import io.holunda.camunda.taskpool.api.business.CreateDataEntryCommand
import io.holunda.camunda.taskpool.api.business.CreateOrUpdateDataEntryCommand
import io.holunda.camunda.taskpool.api.business.UpdateDataEntryCommand
import io.holunda.polyflow.datapool.core.DeletionStrategy
import org.axonframework.commandhandling.CommandHandler
import org.axonframework.eventsourcing.EventSourcingRepository
import org.axonframework.messaging.MetaData
import org.axonframework.modelling.command.Aggregate
import org.axonframework.modelling.command.AggregateNotFoundException
import org.springframework.stereotype.Component
import java.util.*

private val logger = KotlinLogging.logger {}

/**
 * Handler taking care of existence of data entry aggregate.
 */
@Component
class CreateOrUpdateCommandHandler(
  private val eventSourcingRepository: EventSourcingRepository<DataEntryAggregate>,
  private val deletionStrategy: DeletionStrategy
) {

  /**
   * Receives create-or-update and decides what to do.
   * @param command command to create or update the aggregate.
   * @param metaData metadata of the message.
   */
  @CommandHandler
  fun createOrUpdate(command: CreateOrUpdateDataEntryCommand, metaData: MetaData) {
    logger.trace { "Processing createOrUpdate command for ${command.dataIdentity}" }
    // Apply the command only once - either by creating a new aggregate instance or by updating the existing one
    // If a new one is created, it should not be updated immediately afterward with the same data.
    var commandApplied = false
    val aggregate = eventSourcingRepository.loadOrCreate(command.dataIdentity) {
      val createCommand = CreateDataEntryCommand(
        dataEntryChange = command.dataEntryChange
      )
      logger.trace { "No aggregate found. Creating a new data entry aggregate and passing command $command" }
      commandApplied = true
      DataEntryAggregate(
        command = createCommand
      )
    }
    if (!commandApplied) {
      val updateCommand = UpdateDataEntryCommand(
        dataEntryChange = command.dataEntryChange
      )
      logger.trace { "Aggregate found. Updating it passing command $updateCommand" }
      aggregate.invoke {
        it.handle(
          command = updateCommand,
          deletionStrategy = deletionStrategy
        )
      }
    }
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
