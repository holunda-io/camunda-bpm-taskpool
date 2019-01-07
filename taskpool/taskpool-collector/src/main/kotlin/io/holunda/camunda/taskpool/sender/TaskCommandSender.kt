package io.holunda.camunda.taskpool.sender

import io.holunda.camunda.taskpool.api.sender.TaskCommandSender
import io.holunda.camunda.taskpool.api.task.*
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
    sender.send(command)
  }

  /**
   * Sends initial command.
   */
  @EventListener
  override fun sendTaskCommand(command: CreateTaskCommand) {
    sender.send(command)
  }

  /**
   * Update task commands are sent without being enriched.
   */
  @EventListener
  override fun sendTaskCommand(command: UpdateTaskCommand) {
    sender.send(command)
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
