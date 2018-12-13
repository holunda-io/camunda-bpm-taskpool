package io.holunda.camunda.taskpool.sender

import io.holunda.camunda.taskpool.api.sender.TaskCommandSender
import io.holunda.camunda.taskpool.api.task.*
import io.holunda.camunda.taskpool.api.task.CamundaTaskEvent.Companion.ASSIGN
import io.holunda.camunda.taskpool.api.task.CamundaTaskEvent.Companion.ATTRIBUTES
import io.holunda.camunda.taskpool.api.task.CamundaTaskEvent.Companion.CREATE
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

/**
 * Receives task commands from Spring event bus and forwards them to the
 * corresponding sender.
 */
@Component
class TaskCommandCollectorSender(private val sender: CommandSender) : TaskCommandSender {

  /**
   * Sends initial command.
   */
  @EventListener
  override fun sendTaskCommand(command: AssignTaskCommand) {
    sender.send(InitialTaskCommand(
      id = command.id,
      taskDefinitionKey = command.taskDefinitionKey,
      sourceReference = command.sourceReference,
      name = command.name,
      description = command.description,
      priority = command.priority,
      owner = command.owner,
      eventName = ASSIGN,
      dueDate = command.dueDate,
      createTime = command.createTime,
      candidateUsers = command.candidateUsers,
      candidateGroups = command.candidateGroups,
      assignee = command.assignee,
      payload = command.payload,
      businessKey = command.businessKey,
      formKey = command.formKey,
      correlations = command.correlations,
      followUpDate = command.followUpDate,
      enriched = command.enriched
    ))
  }

  /**
   * Sends initial command.
   */
  @EventListener
  override fun sendTaskCommand(command: CreateTaskCommand) {
    sender.send(InitialTaskCommand(
      id = command.id,
      taskDefinitionKey = command.taskDefinitionKey,
      sourceReference = command.sourceReference,
      name = command.name,
      description = command.description,
      priority = command.priority,
      owner = command.owner,
      eventName = CREATE,
      dueDate = command.dueDate,
      createTime = command.createTime,
      candidateUsers = command.candidateUsers,
      candidateGroups = command.candidateGroups,
      assignee = command.assignee,
      payload = command.payload,
      businessKey = command.businessKey,
      formKey = command.formKey,
      correlations = command.correlations,
      followUpDate = command.followUpDate,
      enriched = command.enriched
    ))
  }

  /**
   * Update task commands are sent without being enriched.
   */
  @EventListener
  override fun sendTaskCommand(command: UpdateTaskCommand) {

    when (command) {
      is UpdateAttributeTaskCommand -> sender.send(InitialTaskCommand(
        id = command.id,
        taskDefinitionKey = command.taskDefinitionKey,
        sourceReference = command.sourceReference,
        name = command.name,
        description = command.description,
        priority = command.priority,
        owner = command.owner,
        eventName = ATTRIBUTES,
        dueDate = command.dueDate,
        createTime = command.createTime,
        candidateUsers = command.candidateUsers,
        candidateGroups = command.candidateGroups,
        assignee = command.assignee,
        followUpDate = command.followUpDate
      ))
      is UpdateAssignmentTaskCommand -> sender.send(command)
    }
  }


  /**
   * Sends the complete command.
   */
  @EventListener
  override fun sendTaskCommand(command: CompleteTaskCommand) {
    sender.send(command)
  }

  /**
   * Sends the delete command.
   */
  @EventListener
  override fun sendTaskCommand(command: DeleteTaskCommand) {
    sender.send(command)
  }


}
