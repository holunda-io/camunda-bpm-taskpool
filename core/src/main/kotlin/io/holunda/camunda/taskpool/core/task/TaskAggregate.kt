package io.holunda.camunda.taskpool.core.task

import io.holunda.camunda.taskpool.api.task.*
import mu.KLogging
import org.axonframework.commandhandling.CommandHandler
import org.axonframework.commandhandling.model.AggregateIdentifier
import org.axonframework.commandhandling.model.AggregateLifecycle
import org.axonframework.eventsourcing.EventSourcingHandler
import org.axonframework.spring.stereotype.Aggregate


@Aggregate
open class TaskAggregate() {

  companion object : KLogging()

  @AggregateIdentifier
  private lateinit var id: String
  private var assignee: String? = null
  private var deleted = false
  private var completed = false

  @CommandHandler
  constructor(command: CreateTaskCommand) : this() {
    create(command)
  }

  @CommandHandler
  open fun handle(command: AssignTaskCommand) {
    if (this.assignee != command.assignee) {
      assign(command)
    }
  }

  @CommandHandler
  open fun handle(command: CompleteTaskCommand) {
    if (!deleted && !completed) {
      complete(command)
    }
  }

  @CommandHandler
  fun handle(command: DeleteTaskCommand) {
    if (!deleted && !completed) {
      delete(command)
    }
  }

  @EventSourcingHandler
  open fun on(event: TaskCreatedEvent) {
    this.id = event.id
    logger.debug { "Created task $event" }
  }

  @EventSourcingHandler
  open fun on(event: TaskAssignedEvent) {
    this.assignee = event.assignee
    logger.debug { "Assigned task $this.id to ${this.assignee}" }
  }

  @EventSourcingHandler
  open fun on(event: TaskCompletedEvent) {
    this.completed = true
    logger.debug { "Completed task $this.id by ${this.assignee}" }
  }

  @EventSourcingHandler
  open fun on(event: TaskDeletedEvent) {
    this.deleted = true
    logger.debug { "Deleted task $this.id with reason ${event.deleteReason}" }
  }
}

internal fun assign(command: AssignTaskCommand) =
  AggregateLifecycle.apply(
    TaskAssignedEvent(
      id = command.id,
      taskDefinitionKey = command.taskDefinitionKey,
      caseReference = command.caseReference,
      processReference = command.processReference,
      name = command.name,
      description = command.description,
      formKey = command.formKey,
      priority = command.priority,
      owner = command.owner,
      dueDate = command.dueDate,
      createTime = command.createTime,
      candidateUsers = command.candidateUsers,
      candidateGroups = command.candidateGroups,
      assignee = command.assignee,
      payload = command.payload,
      correlations = command.correlations,
      businessKey = command.businessKey
    ))

internal fun create(command: CreateTaskCommand) =
  AggregateLifecycle.apply(
    TaskCreatedEvent(
      id = command.id,
      taskDefinitionKey = command.taskDefinitionKey,
      caseReference = command.caseReference,
      processReference = command.processReference,
      name = command.name,
      description = command.description,
      formKey = command.formKey,
      priority = command.priority,
      owner = command.owner,
      dueDate = command.dueDate,
      createTime = command.createTime,
      candidateUsers = command.candidateUsers,
      candidateGroups = command.candidateGroups,
      assignee = command.assignee,
      payload = command.payload,
      correlations = command.correlations,
      businessKey = command.businessKey
    ))

internal fun complete(command: CompleteTaskCommand) =
  AggregateLifecycle.apply(
    TaskCompletedEvent(
      id = command.id,
      taskDefinitionKey = command.taskDefinitionKey,
      caseReference = command.caseReference,
      processReference = command.processReference,
      name = command.name,
      description = command.description,
      formKey = command.formKey,
      priority = command.priority,
      owner = command.owner,
      dueDate = command.dueDate,
      createTime = command.createTime,
      candidateUsers = command.candidateUsers,
      candidateGroups = command.candidateGroups,
      assignee = command.assignee,
      payload = command.payload,
      correlations = command.correlations,
      businessKey = command.businessKey
    ))

internal fun delete(command: DeleteTaskCommand) =
  AggregateLifecycle.apply(
    TaskDeletedEvent(
      id = command.id,
      taskDefinitionKey = command.taskDefinitionKey,
      caseReference = command.caseReference,
      processReference = command.processReference,
      name = command.name,
      description = command.description,
      formKey = command.formKey,
      priority = command.priority,
      owner = command.owner,
      dueDate = command.dueDate,
      deleteReason = command.deleteReason,
      createTime = command.createTime,
      candidateUsers = command.candidateUsers,
      candidateGroups = command.candidateGroups,
      assignee = command.assignee,
      payload = command.payload,
      correlations = command.correlations,
      businessKey = command.businessKey
    ))

