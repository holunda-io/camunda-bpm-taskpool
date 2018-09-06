package io.holunda.camunda.taskpool.core

import io.holunda.camunda.taskpool.api.task.*
import mu.KLogging
import org.axonframework.commandhandling.CommandHandler
import org.axonframework.commandhandling.model.AggregateIdentifier
import org.axonframework.commandhandling.model.AggregateLifecycle
import org.axonframework.eventsourcing.EventSourcingHandler
import org.axonframework.spring.stereotype.Aggregate


@Aggregate
class TaskAggregate {

  companion object : KLogging()

  @AggregateIdentifier
  private lateinit var id: String
  private var assignee: String? = null
  private var deleted = false
  private var completed = false

  constructor() {
    // empty constructor for restoring from event store
  }

  @CommandHandler
  constructor(command: CreateTaskCommand) {
    create(command)
  }

  @CommandHandler
  constructor(command: AssignTaskCommand) {
    assign(command)
  }

  @CommandHandler
  fun handle(command: CreateTaskCommand) {
    create(command)
  }

  @CommandHandler
  fun handle(command: AssignTaskCommand) {
    assign(command)
  }

  private fun create(command: CreateTaskCommand) {
    AggregateLifecycle.apply(TaskCreatedEvent(
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
      deleteReason = command.deleteReason,
      createTime = command.createTime,
      candidateUsers = command.candidateUsers,
      candidateGroups = command.candidateGroups,
      assignee = command.assignee,
      payload = command.payload
    ))
  }

  private fun assign(command: AssignTaskCommand) {
    if (this.assignee != command.assignee) {
      AggregateLifecycle.apply(TaskAssignedEvent(
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
        deleteReason = command.deleteReason,
        createTime = command.createTime,
        candidateUsers = command.candidateUsers,
        candidateGroups = command.candidateGroups,
        assignee = command.assignee,
        payload = command.payload
      ))
    }
  }

  @CommandHandler
  fun handle(command: CompleteTaskCommand) {
    if (!deleted && !completed) {
      AggregateLifecycle.apply(TaskCompletedEvent(
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
        deleteReason = command.deleteReason,
        createTime = command.createTime,
        candidateUsers = command.candidateUsers,
        candidateGroups = command.candidateGroups,
        assignee = command.assignee,
        payload = command.payload
      ))
    }
  }

  @CommandHandler
  fun handle(command: DeleteTaskCommand) {
    if (!deleted && !completed) {
      AggregateLifecycle.apply(TaskDeletedEvent(
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
        deleteReason = command.deleteReason,
        createTime = command.createTime,
        candidateUsers = command.candidateUsers,
        candidateGroups = command.candidateGroups,
        assignee = command.assignee,
        payload = command.payload
      ))
    }
  }


  @EventSourcingHandler
  fun on(event: TaskCreatedEvent) {
    this.id = event.id
    this.assignee = event.assignee
    logger.debug { "Created task $event" }
  }

  @EventSourcingHandler
  fun on(event: TaskAssignedEvent) {
    this.assignee = event.assignee
    logger.debug { "Assigned task $this.id to ${this.assignee}" }
  }

  @EventSourcingHandler
  fun on(event: TaskCompletedEvent) {
    this.completed = true
    logger.debug { "Completed task $this.id by ${this.assignee}" }
  }


  @EventSourcingHandler
  fun on(event: TaskDeletedEvent) {
    this.deleted = true
    logger.debug { "Deleted task $this.id with reason ${event.deleteReason}" }
  }

}
