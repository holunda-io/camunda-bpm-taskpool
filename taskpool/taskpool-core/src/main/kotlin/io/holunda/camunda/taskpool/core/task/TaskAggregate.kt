package io.holunda.camunda.taskpool.core.task

import io.holunda.camunda.taskpool.api.business.CorrelationMap
import io.holunda.camunda.taskpool.api.task.*
import mu.KLogging
import org.axonframework.commandhandling.CommandHandler
import org.axonframework.eventsourcing.EventSourcingHandler
import org.axonframework.modelling.command.AggregateIdentifier
import org.axonframework.modelling.command.AggregateLifecycle
import org.axonframework.spring.stereotype.Aggregate
import org.camunda.bpm.engine.variable.VariableMap
import java.util.*


@Aggregate
open class TaskAggregate() {

  companion object : KLogging()

  @AggregateIdentifier

  private lateinit var id: String
  private lateinit var sourceReference: SourceReference
  private lateinit var taskDefinitionKey: String
  private var formKey: String? = null

  private var assignee: String? = null
  private var businessKey: String? = null
  private lateinit var candidateUsers: Set<String>
  private lateinit var candidateGroups: Set<String>
  private lateinit var correlations: CorrelationMap
  private var createTime: Date? = null
  private var description: String? = null
  private var dueDate: Date? = null

  private var followUpDate: Date? = null
  private var name: String? = null
  private var owner: String? = null
  private lateinit var payload: VariableMap
  private var priority: Int? = 0


  private var deleted = false
  private var completed = false


  @CommandHandler
  constructor(command: CreateTaskCommand) : this() {
    logger.debug { "Created new aggregate for task ${command.id}" }
    create(command)
  }

  @CommandHandler
  open fun handle(command: AssignTaskCommand) {
    if (!deleted && !completed) {
      if (assignee != command.assignee) {
        assign(command)
      }
    }
  }

  @CommandHandler
  open fun handle(command: CompleteTaskCommand) {
    if (!deleted && !completed) {
      complete()
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
      logger.debug { "Received updateAttributes intent for task $this.id asState type ${command.javaClass}" }
      updateAttributes(command)
    }
  }

  @CommandHandler
  open fun handle(command: ClaimInteractionTaskCommand) {
    if (!deleted && !completed) {
      if (command.assignee != assignee) {
        // task is assigned to a different user, un-claim it first
        if (assignee != null) {
          unclaim()
        }
        claim(command.assignee)
      }
    }
  }

  @CommandHandler
  open fun handle(command: UnclaimInteractionTaskCommand) {
    if (!deleted && !completed && assignee != null) {
      unclaim()
    }
  }

  @CommandHandler
  open fun handle(command: CompleteInteractionTaskCommand) {
    if (!deleted && !completed) {

      if (command.assignee != null) {
        // task assignment if the assignee is set in the command

        if (command.assignee != this.assignee) {

          if (this.assignee != null) {
            // task is assigned, but to a different user, un-claim it first.
            unclaim()
          }

          // Smart cast is not possible here, because it is a public API declared in a different module.
          claim(command.assignee!!)
        }

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
      undefer()
    }
  }

  /**
   * Add candidate group.
   */
  @CommandHandler
  open fun handle(command: AddCandidateGroupsCommand) {
    if (!deleted && !completed) {
      changeAssignment(command)
    }
  }

  /**
   * Delete candidate group.
   */
  @CommandHandler
  open fun handle(command: DeleteCandidateGroupsCommand) {
    if (!deleted && !completed) {
      changeAssignment(command)
    }
  }

  /**
   * Add candidate user.
   */
  @CommandHandler
  open fun handle(command: AddCandidateUsersCommand) {
    if (!deleted && !completed) {
      changeAssignment(command)
    }
  }

  /**
   * Delete candidate user.
   */
  @CommandHandler
  open fun handle(command: DeleteCandidateUsersCommand) {
    if (!deleted && !completed) {
      changeAssignment(command)
    }
  }


  @EventSourcingHandler
  open fun on(event: TaskCreatedEngineEvent) {
    this.id = event.id
    this.sourceReference = event.sourceReference
    this.taskDefinitionKey = event.taskDefinitionKey
    this.formKey = event.formKey

    this.assignee = event.assignee
    this.businessKey = event.businessKey
    this.candidateGroups = event.candidateGroups
    this.candidateUsers = event.candidateUsers
    this.correlations = event.correlations
    this.createTime = event.createTime
    this.description = event.description
    this.dueDate = event.dueDate
    this.followUpDate = event.followUpDate
    this.name = event.name
    this.owner = event.owner
    this.payload = event.payload
    this.priority = event.priority

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


  private fun assign(command: AssignTaskCommand) =
    AggregateLifecycle.apply(
      TaskAssignedEngineEvent(
        id = this.id,
        taskDefinitionKey = this.taskDefinitionKey,
        sourceReference = this.sourceReference,
        formKey = this.formKey,
        name = this.name,
        description = this.description,
        priority = this.priority,
        owner = this.owner,
        dueDate = this.dueDate,
        createTime = this.createTime,
        candidateUsers = this.candidateUsers,
        candidateGroups = this.candidateGroups,
        assignee = command.assignee,
        payload = this.payload,
        correlations = this.correlations,
        businessKey = this.businessKey,
        followUpDate = this.followUpDate
      ))

  private fun create(command: CreateTaskCommand) =
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

  private fun complete() =
    AggregateLifecycle.apply(
      TaskCompletedEngineEvent(
        id = this.id,
        taskDefinitionKey = this.taskDefinitionKey,
        sourceReference = this.sourceReference,
        formKey = this.formKey,
        name = this.name,
        description = this.description,
        priority = this.priority,
        owner = this.owner,
        dueDate = this.dueDate,
        createTime = this.createTime,
        candidateUsers = this.candidateUsers,
        candidateGroups = this.candidateGroups,
        assignee = this.assignee,
        payload = this.payload,
        correlations = this.correlations,
        businessKey = this.businessKey,
        followUpDate = this.followUpDate
      ))

  private fun delete(command: DeleteTaskCommand) =
    AggregateLifecycle.apply(
      TaskDeletedEngineEvent(
        id = this.id,
        taskDefinitionKey = this.taskDefinitionKey,
        sourceReference = this.sourceReference,
        formKey = this.formKey,
        name = this.name,
        description = this.description,
        priority = this.priority,
        owner = this.owner,
        dueDate = this.dueDate,
        createTime = this.createTime,
        candidateUsers = this.candidateUsers,
        candidateGroups = this.candidateGroups,
        assignee = this.assignee,
        payload = this.payload,
        correlations = this.correlations,
        businessKey = this.businessKey,
        followUpDate = this.followUpDate,

        deleteReason = command.deleteReason
      ))

  private fun updateAttributes(command: UpdateAttributeTaskCommand) {
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
        followUpDate = command.followUpDate
      ))
  }

  private fun claim(assignee: String) =
    AggregateLifecycle.apply(
      TaskClaimedEvent(
        id = this.id,
        taskDefinitionKey = this.taskDefinitionKey,
        sourceReference = this.sourceReference,
        formKey = this.formKey,
        assignee = assignee
      )
    )

  private fun unclaim() =
    AggregateLifecycle.apply(
      TaskUnclaimedEvent(
        id = this.id,
        taskDefinitionKey = this.taskDefinitionKey,
        sourceReference = this.sourceReference,
        formKey = this.formKey
      )
    )

  private fun markToBeCompleted(command: CompleteInteractionTaskCommand) =
    AggregateLifecycle.apply(
      TaskToBeCompletedEvent(
        id = this.id,
        taskDefinitionKey = this.taskDefinitionKey,
        sourceReference = this.sourceReference,
        formKey = this.formKey,
        payload = command.payload
      )
    )

  private fun defer(command: DeferInteractionTaskCommand) =
    AggregateLifecycle.apply(
      TaskDeferredEvent(
        id = this.id,
        taskDefinitionKey = this.taskDefinitionKey,
        sourceReference = this.sourceReference,
        formKey = this.formKey,
        followUpDate = command.followUpDate
      )
    )

  private fun undefer() =
    AggregateLifecycle.apply(
      TaskUndeferredEvent(
        id = this.id,
        taskDefinitionKey = this.taskDefinitionKey,
        sourceReference = this.sourceReference,
        formKey = this.formKey
      )
    )

  // FIXME change representation asState groups and users in events?
  private fun changeAssignment(command: UpdateAssignmentTaskCommand) =
    AggregateLifecycle.apply(
      when (command) {
        is AddCandidateGroupsCommand -> TaskCandidateGroupChanged(
          id = this.id,
          taskDefinitionKey = this.taskDefinitionKey,
          sourceReference = this.sourceReference,
          groupId = command.candidateGroups.first(),
          assignmentUpdateType = CamundaTaskEvent.CANDIDATE_GROUP_ADD
        )
        is DeleteCandidateGroupsCommand -> TaskCandidateGroupChanged(
          id = this.id,
          taskDefinitionKey = this.taskDefinitionKey,
          sourceReference = this.sourceReference,
          groupId = command.candidateGroups.first(),
          assignmentUpdateType = CamundaTaskEvent.CANDIDATE_GROUP_DELETE
        )
        is AddCandidateUsersCommand -> TaskCandidateUserChanged(
          id = this.id,
          taskDefinitionKey = this.taskDefinitionKey,
          sourceReference = this.sourceReference,
          userId = command.candidateUsers.first(),
          assignmentUpdateType = CamundaTaskEvent.CANDIDATE_USER_ADD
        )
        is DeleteCandidateUsersCommand -> TaskCandidateUserChanged(
          id = this.id,
          taskDefinitionKey = this.taskDefinitionKey,
          sourceReference = this.sourceReference,
          userId = command.candidateUsers.first(),
          assignmentUpdateType = CamundaTaskEvent.CANDIDATE_USER_DELETE
        )
      }
    )
}
