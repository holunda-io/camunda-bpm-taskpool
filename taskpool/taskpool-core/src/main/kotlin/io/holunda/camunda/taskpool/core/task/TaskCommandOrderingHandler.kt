package io.holunda.camunda.taskpool.core.task

import io.holunda.camunda.taskpool.api.task.AssignTaskCommand
import io.holunda.camunda.taskpool.api.task.CamundaTaskEvent.Companion.ASSIGN
import io.holunda.camunda.taskpool.api.task.CamundaTaskEvent.Companion.ATTRIBUTES
import io.holunda.camunda.taskpool.api.task.CreateTaskCommand
import io.holunda.camunda.taskpool.api.task.InitialTaskCommand
import io.holunda.camunda.taskpool.api.task.UpdateAttributeTaskCommand
import mu.KLogging
import org.axonframework.commandhandling.CommandHandler
import org.axonframework.eventsourcing.EventSourcingRepository
import org.axonframework.modelling.command.Aggregate
import org.axonframework.modelling.command.AggregateNotFoundException
import org.springframework.stereotype.Component
import java.util.*

@Component
open class TaskCommandOrderingHandler(private val eventSourcingRepository: EventSourcingRepository<TaskAggregate>) {

  companion object : KLogging()

  @CommandHandler
  open fun handle(command: InitialTaskCommand) {

    logger.debug { "Received command $command, delegating to the aggregate" }

    getAggregateById(command.id)
      .orElseGet {
        eventSourcingRepository.newInstance {
          TaskAggregate(create(command))
        }
      }
      .invoke {
        when (command.eventName) {
          ASSIGN -> it.handle(assign(command))
          ATTRIBUTES -> it.handle(update(command))
        }

      }
  }

  fun getAggregateById(id: String): Optional<Aggregate<TaskAggregate>> {
    return try {
      Optional.of(eventSourcingRepository.load(id))
    } catch (ex: AggregateNotFoundException) {
      Optional.empty()
    }
  }

  fun assign(command: InitialTaskCommand): AssignTaskCommand =
    AssignTaskCommand(
      id = command.id,
      taskDefinitionKey = command.taskDefinitionKey,
      sourceReference = command.sourceReference,

      name = command.name,
      description = command.description,
      priority = command.priority,
      owner = command.owner,
      eventName = command.eventName,
      enriched = command.enriched,
      dueDate = command.dueDate,
      followUpDate = command.followUpDate,
      createTime = command.createTime,
      candidateUsers = command.candidateUsers,
      candidateGroups = command.candidateGroups,
      assignee = command.assignee,
      payload = command.payload,
      businessKey = command.businessKey,
      correlations = command.correlations
    )

  fun create(command: InitialTaskCommand): CreateTaskCommand =
    CreateTaskCommand(
      id = command.id,
      taskDefinitionKey = command.taskDefinitionKey,
      sourceReference = command.sourceReference,

      name = command.name,
      description = command.description,
      priority = command.priority,
      owner = command.owner,
      eventName = command.eventName,
      enriched = command.enriched,
      dueDate = command.dueDate,
      followUpDate = command.followUpDate,
      createTime = command.createTime,
      candidateUsers = command.candidateUsers,
      candidateGroups = command.candidateGroups,
      assignee = command.assignee,
      payload = command.payload,
      businessKey = command.businessKey,
      formKey = command.formKey,
      correlations = command.correlations
    )

  fun update(command: InitialTaskCommand): UpdateAttributeTaskCommand =
    UpdateAttributeTaskCommand(
      id = command.id,
      taskDefinitionKey = command.taskDefinitionKey,
      sourceReference = command.sourceReference,

      name = command.name,
      description = command.description,
      priority = command.priority,
      owner = command.owner,
      dueDate = command.dueDate,
      followUpDate = command.followUpDate,
      assignee = command.assignee
    )

}
