package io.holunda.camunda.taskpool.sender

import io.holunda.camunda.taskpool.TaskCollectorProperties
import io.holunda.camunda.taskpool.api.task.AssignTaskCommand
import io.holunda.camunda.taskpool.api.task.CompleteTaskCommand
import io.holunda.camunda.taskpool.api.task.CreateTaskCommand
import io.holunda.camunda.taskpool.api.task.DeleteTaskCommand
import org.axonframework.commandhandling.gateway.CommandGateway
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component


@Component
class TaskCommandSender(
  private val gateway: CommandGateway,
  private val properties: TaskCollectorProperties
) {

  @EventListener(condition = "#command.enriched")
  fun enrich(command: CreateTaskCommand) {
    send(command)
  }

  @EventListener(condition = "#command.enriched")
  fun enrich(command: CompleteTaskCommand) {
    send(command)
  }

  @EventListener(condition = "#command.enriched")
  fun enrich(command: DeleteTaskCommand) {
    send(command)
  }

  @EventListener(condition = "#command.enriched")
  fun enrich(command: AssignTaskCommand) {
    send(command)
  }

  private fun send(command: Any) {
    if (properties.sender.enabled) {
      gateway.send<Any>(command)
    }
  }

}
