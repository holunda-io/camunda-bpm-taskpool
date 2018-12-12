package io.holunda.camunda.taskpool.core.task

import io.holunda.camunda.taskpool.api.task.*
import mu.KLogging
import org.axonframework.commandhandling.CommandHandler
import org.axonframework.eventsourcing.EventSourcingHandler
import org.axonframework.modelling.command.AggregateIdentifier
import org.axonframework.modelling.command.AggregateLifecycle
import org.axonframework.spring.stereotype.Aggregate


@Aggregate
open class TaskAggregate() {

  companion object : KLogging()

  @AggregateIdentifier
  private lateinit var id: String
  private lateinit var sourceReference: SourceReference
  private lateinit var taskDefinitionKey: String
  private var formKey: String? = null
  private var assignee: String? = null
  private var deleted = false
  private var completed = false

  @CommandHandler
  constructor(command: CreateTaskCommand) : this() {
    logger.debug { "Created new aggregate for task ${command.id}" }
    create(command)
  }

  @CommandHandler
  open fun handle(command: AssignTaskCommand) {
    if (assignee != command.assignee) {
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
  open fun handle(command: DeleteTaskCommand) {
    if (!deleted && !completed) {
      delete(command)
    }
  }

  @CommandHandler
  open fun handle(command: UpdateAttributeTaskCommand) {
    if (!deleted && !completed) {
      logger.debug { "Received updateAttributes intent for task $this.id of type ${command.javaClass}" }
      updateAttributes(command)
    }
  }

  @CommandHandler
  open fun handle(command: ClaimInteractionTaskCommand) {
    if (!deleted && !completed) {
      if (assignee != null) {
        // task is assigned, unclaim it first
        unclaim(command)
      }
      claim(command, command.assignee)
    }
  }

  @CommandHandler
  open fun handle(command: UnclaimInteractionTaskCommand) {
    if (!deleted && !completed && assignee != null) {
      unclaim(command)
    }
  }

  @CommandHandler
  open fun handle(command: CompleteInteractionTaskCommand) {
    if (!deleted && !completed) {
      if (command.assignee != null) {
        if (assignee != null) {
          unclaim(command)
        }
        // Smart cast is not possible here, because it is a public API declared in a different module.
        claim(command, command.assignee!!)
      }
      markToBeCompleted(command)
    }
  }

  @CommandHandler
  open fun handle(command: DeferInteractionTaskCommand) {
    if (!deleted && !completed) {
      defer(command)
    }
  }

  @CommandHandler
  open fun handle(command: UndeferInteractionTaskCommand) {
    if (!deleted && !completed) {
      undefer(command)
    }
  }

  @EventSourcingHandler
  open fun on(event: TaskCreatedEngineEvent) {
    this.id = event.id
    this.assignee = event.assignee
    this.sourceReference = event.sourceReference
    this.taskDefinitionKey = event.taskDefinitionKey
    this.formKey = event.formKey
    logger.debug { "Created task $event" }
  }

  @EventSourcingHandler
  open fun on(event: TaskAssignedEngineEvent) {
    this.assignee = event.assignee
    logger.debug { "Assigned task $this.id to $assignee" }
  }

  @EventSourcingHandler
  open fun on(event: TaskCompletedEngineEvent) {
    this.completed = true
    logger.debug { "Completed task $this.id by $assignee" }
  }

  @EventSourcingHandler
  open fun on(event: TaskDeletedEngineEvent) {
    this.deleted = true
    logger.debug { "Deleted task $this.id with reason ${event.deleteReason}" }
  }

  internal fun assign(command: AssignTaskCommand) =
    AggregateLifecycle.apply(
      TaskAssignedEngineEvent(
        id = this.id,
        taskDefinitionKey = this.taskDefinitionKey,
        sourceReference = this.sourceReference,
        formKey = this.formKey,
        name = command.name,
        description = command.description,
        priority = command.priority,
        owner = command.owner,
        dueDate = command.dueDate,
        createTime = command.createTime,
        candidateUsers = command.candidateUsers,
        candidateGroups = command.candidateGroups,
        assignee = command.assignee,
        payload = command.payload,
        correlations = command.correlations,
        businessKey = command.businessKey,
        followUpDate = command.followUpDate
      ))

  internal fun create(command: CreateTaskCommand) =
    AggregateLifecycle.apply(
      TaskCreatedEngineEvent(
        id = command.id,
        taskDefinitionKey = command.taskDefinitionKey,
        sourceReference = command.sourceReference,
        formKey = command.formKey,
        name = command.name,
        description = command.description,
        priority = command.priority,
        owner = command.owner,
        dueDate = command.dueDate,
        createTime = command.createTime,
        candidateUsers = command.candidateUsers,
        candidateGroups = command.candidateGroups,
        assignee = command.assignee,
        payload = command.payload,
        correlations = command.correlations,
        businessKey = command.businessKey,
        followUpDate = command.followUpDate
      ))

  internal fun complete(command: CompleteTaskCommand) =
    AggregateLifecycle.apply(
      TaskCompletedEngineEvent(
        id = this.id,
        taskDefinitionKey = this.taskDefinitionKey,
        sourceReference = this.sourceReference,
        formKey = this.formKey,
        name = command.name,
        description = command.description,
        priority = command.priority,
        owner = command.owner,
        dueDate = command.dueDate,
        createTime = command.createTime,
        candidateUsers = command.candidateUsers,
        candidateGroups = command.candidateGroups,
        assignee = command.assignee,
        payload = command.payload,
        correlations = command.correlations,
        businessKey = command.businessKey,
        followUpDate = command.followUpDate
      ))

  internal fun delete(command: DeleteTaskCommand) =
    AggregateLifecycle.apply(
      TaskDeletedEngineEvent(
        id = this.id,
        taskDefinitionKey = this.taskDefinitionKey,
        sourceReference = this.sourceReference,
        formKey = this.formKey,
        name = command.name,
        description = command.description,
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
        businessKey = command.businessKey,
        followUpDate = command.followUpDate
      ))

  internal fun updateAttributes(command: UpdateAttributeTaskCommand) {
    AggregateLifecycle.apply(
      TaskAttributeUpdatedEngineEvent(
        id = this.id,
        taskDefinitionKey = this.taskDefinitionKey,
        sourceReference = this.sourceReference,
        name = command.name,
        description = command.description,
        priority = command.priority,
        owner = command.owner,
        dueDate = command.dueDate,
        followUpDate = command.followUpDate,
        assignee = command.assignee
      ))
  }

  fun claim(command: InteractionTaskCommand, assignee: String) =
    AggregateLifecycle.apply(
      TaskClaimedEvent(
        id = this.id,
        taskDefinitionKey = this.taskDefinitionKey,
        sourceReference = this.sourceReference,
        formKey = this.formKey,
        assignee = assignee
      )
    )

  fun unclaim(command: InteractionTaskCommand) =
    AggregateLifecycle.apply(
      TaskUnclaimedEvent(
        id = this.id,
        taskDefinitionKey = this.taskDefinitionKey,
        sourceReference = this.sourceReference,
        formKey = this.formKey
      )
    )

  fun markToBeCompleted(command: CompleteInteractionTaskCommand) =
    AggregateLifecycle.apply(
      TaskToBeCompletedEvent(
        id = this.id,
        taskDefinitionKey = this.taskDefinitionKey,
        sourceReference = this.sourceReference,
        formKey = this.formKey,
        payload = command.payload
      )
    )

  fun defer(command: DeferInteractionTaskCommand) =
    AggregateLifecycle.apply(
      TaskDeferredEvent(
        id = this.id,
        taskDefinitionKey = this.taskDefinitionKey,
        sourceReference = this.sourceReference,
        formKey = this.formKey,
        followUpDate = command.followUpDate
      )
    )

  fun undefer(command: UndeferInteractionTaskCommand) =
    AggregateLifecycle.apply(
      TaskUndeferredEvent(
        id = this.id,
        taskDefinitionKey = this.taskDefinitionKey,
        sourceReference = this.sourceReference,
        formKey = this.formKey
      )
    )
}
