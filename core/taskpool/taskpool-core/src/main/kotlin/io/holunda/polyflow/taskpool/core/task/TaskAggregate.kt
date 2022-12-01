package io.holunda.polyflow.taskpool.core.task

import io.holunda.camunda.taskpool.api.task.*
import io.holunda.camunda.taskpool.mapper.task.*
import io.holunda.camunda.taskpool.model.Task
import io.holunda.polyflow.taskpool.core.TaskPoolCoreConfiguration
import mu.KLogging
import org.axonframework.commandhandling.CommandHandler
import org.axonframework.eventsourcing.EventSourcingHandler
import org.axonframework.modelling.command.AggregateIdentifier
import org.axonframework.modelling.command.AggregateLifecycle
import org.axonframework.spring.stereotype.Aggregate

/**
 * Main representation of the tasks available in the system.
 */
@Aggregate(repository = TaskPoolCoreConfiguration.TASK_AGGREGATE_REPOSITORY)
class TaskAggregate() {

  companion object : KLogging()

  @AggregateIdentifier
  private lateinit var id: String
  internal lateinit var task: Task

  private var deleted = false
  private var completed = false

  /**
   * This handler triggers on a duplication of creation and
   * is invoked manually on aggregate creation and is therefor
   * not annotated with {@link CommandHandler}
   */
  fun handle(command: CreateTaskCommand) {
    logger.debug { "Created new aggregate for task ${command.id}" }
    AggregateLifecycle.apply(
      Task.from(command).createdEvent().also { logger.debug { "Created task $it" } }
    )
  }

  /**
   * User assignment handler.
   */
  @CommandHandler
  fun handle(command: AssignTaskCommand) {
    if (!deleted && !completed) {
      if (task.assignee != command.assignee) {
        AggregateLifecycle.apply(
          task.assignedEvent(command.assignee).also { logger.debug { "Assigned task ${it.id} to ${it.assignee}" } }
        )
      }
    }
  }

  /**
   * Task completion handler.
   */
  @CommandHandler
  fun handle(command: CompleteTaskCommand) {
    if (!deleted && !completed) {
      if (command.assignee != null) {
        AggregateLifecycle.apply(
          task.assignedEvent(command.assignee)
        )
      }
      AggregateLifecycle.apply(
        task.completedEvent().also { logger.debug { "Completed task ${it.id} by $it.assignee" } }
      )
    }
  }

  /**
   * Task deletion handler.
   */
  @CommandHandler
  fun handle(command: DeleteTaskCommand) {
    if (!deleted && !completed) {
      AggregateLifecycle.apply(
        task.deletedEvent(command.deleteReason).also { logger.debug { "Deleted task ${it.id} with reason ${it.deleteReason}" } }
      )
    }
  }

  /**
   * Handles task attribute update.
   */
  @CommandHandler
  fun handle(command: UpdateAttributeTaskCommand) {
    if (!deleted && !completed) {
      AggregateLifecycle.apply(
        task.updateAttributesEvent(Task.from(command), command.enriched).also {
          logger.debug { "Received updateAttributes intent for task $task.id of type ${command.javaClass}" }
        }
      )
    }
  }

  /**
   * Handles intent to claim the task (sent by the user).
   */
  @CommandHandler
  fun handle(command: ClaimInteractionTaskCommand) {
    if (!deleted && !completed) {
      if (command.assignee != task.assignee) {
        // task is assigned to a different user, un-claim it first
        if (task.assignee != null) {
          AggregateLifecycle.apply(
            task.unclaimedEvent()
          )
        }
        AggregateLifecycle.apply(
          task.claimedEvent(command.assignee)
        )
      }
    }
  }

  /**
   * Handles intent to un-claim the task (sent by the user).
   */
  @CommandHandler
  fun handle(command: UnclaimInteractionTaskCommand) {
    if (!deleted && !completed && task.assignee != null) {
      AggregateLifecycle.apply(
        task.unclaimedEvent()
      )
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

        if (command.assignee != task.assignee) {

          if (task.assignee != null) {
            // task is assigned, but to a different user, un-claim it first.
            AggregateLifecycle.apply(
              task.unclaimedEvent()
            )
          }
          // Smart cast is not possible here, because it is a public API declared in a different module.
          AggregateLifecycle.apply(
            task.claimedEvent(command.assignee!!)
          )
        }
      }
      AggregateLifecycle.apply(
        task.markToBeCompletedEvent(command.payload)
      )
    }
  }

  /**
   * Handles intent to defer the task (sent by the user).
   */
  @CommandHandler
  fun handle(command: DeferInteractionTaskCommand) {
    if (!deleted && !completed) {
      AggregateLifecycle.apply(
        task.deferredEvent(command.followUpDate)
      )
    }
  }

  /**
   * Handles intent to undefer the task (sent by the user).
   */
  @CommandHandler
  fun handle(command: UndeferInteractionTaskCommand) {
    if (!deleted && !completed) {
      AggregateLifecycle.apply(
        task.undeferredEvent()
      )
    }
  }

  /**
   * Add candidate group.
   */
  @CommandHandler
  fun handle(command: AddCandidateGroupsCommand) {
    if (!deleted && !completed) {
      command.candidateGroups.map { groupId ->
        AggregateLifecycle.apply(
          task.addCandidateGroupEvent(groupId = groupId)
        )
      }
    }
  }

  /**
   * Delete candidate group.
   */
  @CommandHandler
  fun handle(command: DeleteCandidateGroupsCommand) {
    if (!deleted && !completed) {
      command.candidateGroups.map { groupId ->
        AggregateLifecycle.apply(
          task.removeCandidateGroupEvent(groupId = groupId)
        )
      }
    }
  }

  /**
   * Add candidate user.
   */
  @CommandHandler
  fun handle(command: AddCandidateUsersCommand) {
    if (!deleted && !completed) {
      command.candidateUsers.map { userId ->
        AggregateLifecycle.apply(
          task.addCandidateUserEvent(userId = userId)
        )
      }
    }
  }

  /**
   * Delete candidate user.
   */
  @CommandHandler
  fun handle(command: DeleteCandidateUsersCommand) {
    if (!deleted && !completed) {
      command.candidateUsers.map { userId ->
        AggregateLifecycle.apply(
          task.removeCandidateUserEvent(userId = userId)
        )
      }
    }
  }

  /**
   * React on task creation.
   */
  @EventSourcingHandler
  fun on(event: TaskCreatedEngineEvent) {
    this.id = event.id // axon framework requirement
    this.task = Task.from(event)
  }

  /**
   * React on attribute updates.
   */
  @EventSourcingHandler
  fun on(event: TaskAttributeUpdatedEngineEvent) {
    this.task.update(Task.from(event))
  }

  /**
   * React on task assignment.
   */
  @EventSourcingHandler
  fun on(event: TaskAssignedEngineEvent) {
    this.task.assignee = event.assignee
  }

  /**
   * React on task completion.
   */
  @EventSourcingHandler
  fun on(event: TaskCompletedEngineEvent) {
    this.completed = true
  }

  /**
   * React on task deletion.
   */
  @EventSourcingHandler
  fun on(event: TaskDeletedEngineEvent) {
    this.deleted = true
  }

}
