package io.holunda.polyflow.taskpool.core.task

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

/**
 * Main representation of the tasks available in the system.
 */
@Aggregate
class TaskAggregate() {

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

  /**
   * This handler triggers on a duplication of creation and
   * is invoked manually on aggregate creation and is therefor
   * not annotated with {@link CommandHandler}
   */
  fun handle(command: CreateTaskCommand) {
    logger.debug { "Created new aggregate for task ${command.id}" }
    create(command)
  }

  /**
   * User assignment handler.
   */
  @CommandHandler
  fun handle(command: AssignTaskCommand) {
    if (!deleted && !completed) {
      if (assignee != command.assignee) {
        assign(command)
      }
    }
  }

  /**
   * Task completion handler.
   */
  @CommandHandler
  fun handle(command: CompleteTaskCommand) {
    if (!deleted && !completed) {
      complete(command.assignee)
    }
  }

  /**
   * Task deletion handler.
   */
  @CommandHandler
  fun handle(command: DeleteTaskCommand) {
    if (!deleted && !completed) {
      delete(command)
    }
  }

  /**
   * Handles task attribute update.
   */
  @CommandHandler
  fun handle(command: UpdateAttributeTaskCommand) {
    if (!deleted && !completed) {
      logger.debug { "Received updateAttributes intent for task $this.id of type ${command.javaClass}" }
      updateAttributes(command)
    }
  }

  /**
   * Handles intent to claim the task (sent by the user).
   */
  @CommandHandler
  fun handle(command: ClaimInteractionTaskCommand) {
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

  /**
   * Handles intent to un-claim the task (sent by the user).
   */
  @CommandHandler
  fun handle(command: UnclaimInteractionTaskCommand) {
    if (!deleted && !completed && assignee != null) {
      unclaim()
    }
  }

  /**
   * Handles intent to complete the task (sent by the user).
   */
  @CommandHandler
  fun handle(command: CompleteInteractionTaskCommand) {
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

  /**
   * Handles intent to defer the task (sent by the user).
   */
  @CommandHandler
  fun handle(command: DeferInteractionTaskCommand) {
    if (!deleted && !completed) {
      defer(command)
    }
  }

  /**
   * Handles intent to undefer the task (sent by the user).
   */
  @CommandHandler
  fun handle(command: UndeferInteractionTaskCommand) {
    if (!deleted && !completed) {
      undefer()
    }
  }

  /**
   * Add candidate group.
   */
  @CommandHandler
  fun handle(command: AddCandidateGroupsCommand) {
    if (!deleted && !completed) {
      changeAssignment(command)
    }
  }

  /**
   * Delete candidate group.
   */
  @CommandHandler
  fun handle(command: DeleteCandidateGroupsCommand) {
    if (!deleted && !completed) {
      changeAssignment(command)
    }
  }

  /**
   * Add candidate user.
   */
  @CommandHandler
  fun handle(command: AddCandidateUsersCommand) {
    if (!deleted && !completed) {
      changeAssignment(command)
    }
  }

  /**
   * Delete candidate user.
   */
  @CommandHandler
  fun handle(command: DeleteCandidateUsersCommand) {
    if (!deleted && !completed) {
      changeAssignment(command)
    }
  }

  @EventSourcingHandler
  fun on(event: TaskCreatedEngineEvent) {
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
  }

  @EventSourcingHandler
  fun on(event: TaskAssignedEngineEvent) {
    this.assignee = event.assignee
  }

  @EventSourcingHandler
  fun on(event: TaskCompletedEngineEvent) {
    this.completed = true
  }

  @EventSourcingHandler
  fun on(event: TaskDeletedEngineEvent) {
    this.deleted = true
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
      ).also { logger.debug { "Assigned task ${it.id} to ${it.assignee}" } }
    )

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
      ).also { logger.debug { "Created task $it" } }
    )

  private fun complete(assignee: String?) {
    if (assignee != null) {
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
          assignee = assignee,
          payload = this.payload,
          correlations = this.correlations,
          businessKey = this.businessKey,
          followUpDate = this.followUpDate
        )
      )
    }
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
      ).also { logger.debug { "Completed task ${it.id} by $it.assignee" } }
    )
  }

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
      ).also { logger.debug { "Deleted task ${it.id} with reason ${it.deleteReason}" } }
    )

  private fun updateAttributes(command: UpdateAttributeTaskCommand) {
    AggregateLifecycle.apply(
      TaskAttributeUpdatedEngineEvent(
        id = this.id,
        taskDefinitionKey = command.taskDefinitionKey,
        sourceReference = command.sourceReference,
        name = command.name,
        description = command.description,
        priority = command.priority,
        owner = command.owner,
        dueDate = command.dueDate,
        followUpDate = command.followUpDate,
        businessKey = this.businessKey,
        correlations = if (command.enriched) { command.correlations } else { this.correlations },
        payload = if (command.enriched) { command.payload } else { this.payload }
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

  // FIXME change representation of groups and users in events?
  private fun changeAssignment(command: UpdateAssignmentTaskCommand) =
    AggregateLifecycle.apply(
      when (command) {
        is AddCandidateGroupsCommand -> TaskCandidateGroupChanged(
          id = this.id,
          taskDefinitionKey = this.taskDefinitionKey,
          sourceReference = this.sourceReference,
          groupId = command.candidateGroups.first(),
          assignmentUpdateType = CamundaTaskEventType.CANDIDATE_GROUP_ADD
        )
        is DeleteCandidateGroupsCommand -> TaskCandidateGroupChanged(
          id = this.id,
          taskDefinitionKey = this.taskDefinitionKey,
          sourceReference = this.sourceReference,
          groupId = command.candidateGroups.first(),
          assignmentUpdateType = CamundaTaskEventType.CANDIDATE_GROUP_DELETE
        )
        is AddCandidateUsersCommand -> TaskCandidateUserChanged(
          id = this.id,
          taskDefinitionKey = this.taskDefinitionKey,
          sourceReference = this.sourceReference,
          userId = command.candidateUsers.first(),
          assignmentUpdateType = CamundaTaskEventType.CANDIDATE_USER_ADD
        )
        is DeleteCandidateUsersCommand -> TaskCandidateUserChanged(
          id = this.id,
          taskDefinitionKey = this.taskDefinitionKey,
          sourceReference = this.sourceReference,
          userId = command.candidateUsers.first(),
          assignmentUpdateType = CamundaTaskEventType.CANDIDATE_USER_DELETE
        )
      }
    )
}
