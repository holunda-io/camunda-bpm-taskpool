package io.holunda.camunda.taskpool.core.task

import io.holunda.camunda.taskpool.api.task.AssignTaskCommand
import io.holunda.camunda.taskpool.api.task.CreateOrAssignTaskCommand
import io.holunda.camunda.taskpool.api.task.CreateTaskCommand
import org.axonframework.commandhandling.CommandHandler
import org.axonframework.commandhandling.model.Aggregate
import org.axonframework.commandhandling.model.AggregateNotFoundException
import org.axonframework.eventsourcing.EventSourcingRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.util.*

@Component
open class CreateOrAssignCommandHandler() {

  @Autowired
  private lateinit var eventSourcingRepository: EventSourcingRepository<TaskAggregate>

  @CommandHandler
  open fun createOrAssign(command: CreateOrAssignTaskCommand) {
    getAggregateById(command.id)
      .orElseGet {
        eventSourcingRepository.newInstance {
          TaskAggregate(
            CreateTaskCommand(
              id = command.id,
              taskDefinitionKey = command.taskDefinitionKey,
              caseReference = command.caseReference,
              processReference = command.processReference,
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
              formKey = command.formKey
            ))
        }
      }
      .invoke {
        it.handle(
          AssignTaskCommand(
            id = command.id,
            taskDefinitionKey = command.taskDefinitionKey,
            caseReference = command.caseReference,
            processReference = command.processReference,
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
            formKey = command.formKey
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
