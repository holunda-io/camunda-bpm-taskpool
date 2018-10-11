package io.holunda.camunda.taskpool.core.business

import io.holunda.camunda.taskpool.api.business.CreateDataEntryCommand
import io.holunda.camunda.taskpool.api.business.CreateOrUpdateDataEntryCommand
import io.holunda.camunda.taskpool.api.business.UpdateDataEntryCommand
import io.holunda.camunda.taskpool.core.ifPresentOrElse
import org.axonframework.commandhandling.CommandHandler
import org.axonframework.commandhandling.model.Aggregate
import org.axonframework.commandhandling.model.AggregateNotFoundException
import org.axonframework.eventsourcing.EventSourcingRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.util.*

@Component
open class CreateOrUpdateCommandHandler {

  @Autowired
  private lateinit var eventSourcingRepository: EventSourcingRepository<DataEntryAggregate>

  @CommandHandler
  open fun createOrUpdate(command: CreateOrUpdateDataEntryCommand) {

    loadAggregate(command.dataIdentity).ifPresentOrElse(
      presentConsumer = { aggregate ->
        aggregate.invoke {
          it.handle(
            UpdateDataEntryCommand(
              entryType = command.entryType,
              entryId = command.entryId,
              correlations = command.correlations,
              payload = command.payload)
          )
        }
      },
      missingCallback = {
        eventSourcingRepository.newInstance {
          DataEntryAggregate(
            CreateDataEntryCommand(
              entryType = command.entryType,
              entryId = command.entryId,
              correlations = command.correlations,
              payload = command.payload)
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
