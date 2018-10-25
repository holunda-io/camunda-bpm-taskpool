package io.holunda.camunda.taskpool.core.task

import io.holunda.camunda.taskpool.api.task.AssignTaskCommand
import io.holunda.camunda.taskpool.api.task.CreateOrAssignTaskCommand
import io.holunda.camunda.taskpool.api.task.CreateTaskCommand
import mu.KLogging
import org.axonframework.commandhandling.CommandHandler
import org.axonframework.eventsourcing.EventSourcingRepository
import org.axonframework.modelling.command.Aggregate
import org.axonframework.modelling.command.AggregateNotFoundException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.util.*

@Component
open class CreateOrAssignCommandHandler {

  companion object: KLogging()

  @Autowired
  private lateinit var eventSourcingRepository: EventSourcingRepository<TaskAggregate>

  @CommandHandler
  open fun createOrAssign(command: CreateOrAssignTaskCommand) {

    logger.info { "Received command $command, delegating to the aggregate" }

    getAggregateById(command.id)
      .orElseGet {
        eventSourcingRepository.newInstance {
          TaskAggregate(
            CreateTaskCommand(
              id = command.id,
              taskDefinitionKey = command.taskDefinitionKey,
              sourceReference = command.sourceReference,
              name = command.name,
              description = command.description,
              priority = command.priority,
              owner = command.owner,
              eventName = command.eventName,
              dueDate = command.dueDate,
              createTime = command.createTime,
              candidateUsers = command.candidateUsers,
              candidateGroups = command.candidateGroups,
              assignee = command.assignee,
              payload = command.payload,
              businessKey = command.businessKey,
              formKey = command.formKey,
              correlations = command.correlations
            ))
        }
      }
      .invoke {
        it.handle(
          AssignTaskCommand(
            id = command.id,
            taskDefinitionKey = command.taskDefinitionKey,
            sourceReference = command.sourceReference,
            name = command.name,
            description = command.description,
            priority = command.priority,
            owner = command.owner,
            eventName = command.eventName,
            dueDate = command.dueDate,
            createTime = command.createTime,
            candidateUsers = command.candidateUsers,
            candidateGroups = command.candidateGroups,
            assignee = command.assignee,
            payload = command.payload,
            businessKey = command.businessKey,
            formKey = command.formKey,
            correlations = command.correlations
          ))
      }
  }

  private fun getAggregateById(id: String): Optional<Aggregate<TaskAggregate>> {
    return try {
      Optional.of(eventSourcingRepository.load(id))
    } catch (ex: AggregateNotFoundException) {
      Optional.empty()
    }
  }
}
