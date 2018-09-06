package io.holunda.camunda.taskpool.sender

import io.holunda.camunda.taskpool.api.task.AssignTaskCommand
import io.holunda.camunda.taskpool.api.task.CompleteTaskCommand
import io.holunda.camunda.taskpool.api.task.CreateTaskCommand
import io.holunda.camunda.taskpool.api.task.DeleteTaskCommand
import org.axonframework.commandhandling.gateway.CommandGateway
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component


@Component
class TaskCommandSender(private val gateway: CommandGateway) {

  @EventListener(condition = "#command.enriched")
  fun enrich(command: CreateTaskCommand) {
    gateway.send<Any>(command)
  }

  @EventListener(condition = "#command.enriched")
  fun enrich(command: CompleteTaskCommand) {
    gateway.send<Any>(command)
  }

  @EventListener(condition = "#command.enriched")
  fun enrich(command: DeleteTaskCommand) {
    gateway.send<Any>(command)
  }

  @EventListener(condition = "#command.enriched")
  fun enrich(command: AssignTaskCommand) {
    gateway.send<Any>(command)
  }

}
