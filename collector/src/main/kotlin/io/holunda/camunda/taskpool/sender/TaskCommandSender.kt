package io.holunda.camunda.taskpool.sender

import io.holunda.camunda.taskpool.TaskCollectorProperties
import io.holunda.camunda.taskpool.api.task.*
import mu.KLogging
import org.axonframework.commandhandling.CommandCallback
import org.axonframework.commandhandling.CommandMessage
import org.axonframework.commandhandling.gateway.CommandGateway
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component


@Component
class TaskCommandSender(
  private val gateway: CommandGateway,
  private val properties: TaskCollectorProperties
) {

  companion object : KLogging()

  @EventListener(condition = "#command.enriched")
  fun sendTaskCommand(command: AssignTaskCommand) {
    send(CreateOrAssignTaskCommand(
      id = command.id,
      taskDefinitionKey = command.taskDefinitionKey,
      sourceReference = command.sourceReference,
      name = command.name,
      description = command.description,
      priority = command.priority,
      owner = command.owner,
      eventName = "assignment",
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

  @EventListener(condition = "#command.enriched")
  fun sendTaskCommand(command: CreateTaskCommand) {
    send(CreateOrAssignTaskCommand(
      id = command.id,
      taskDefinitionKey = command.taskDefinitionKey,
      sourceReference = command.sourceReference,
      name = command.name,
      description = command.description,
      priority = command.priority,
      owner = command.owner,
      eventName = "create",
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

  @EventListener(condition = "#command.enriched")
  fun sendTaskCommand(command: CompleteTaskCommand) {
    send(command)
  }

  @EventListener(condition = "#command.enriched")
  fun sendTaskCommand(command: DeleteTaskCommand) {
    send(command)
  }

  private fun send(command: Any) {
    if (properties.sender.enabled) {
      gateway.send<Any, Any?>(command, object : CommandCallback<Any, Any?> {
        override fun onSuccess(m: CommandMessage<out Any>, result: Any?) {
          logger.debug("Successfully submitted command $command")
        }

        override fun onFailure(message: CommandMessage<out Any>?, e: Throwable) {
          logger.error("Error sending command $message", e)
        }
      })
    } else {
      logger.debug("Would have sent command $command")
    }
  }

}
