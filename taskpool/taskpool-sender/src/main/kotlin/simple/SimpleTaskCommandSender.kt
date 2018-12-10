package io.holunda.camunda.taskpool.sender.simple

import io.holunda.camunda.taskpool.TaskCollectorProperties
import io.holunda.camunda.taskpool.api.sender.TaskCommandSender
import io.holunda.camunda.taskpool.api.task.*
import org.axonframework.commandhandling.gateway.CommandGateway
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
class SimpleTaskCommandSender(
  private val gateway: CommandGateway,
  private val properties: TaskCollectorProperties
) : TaskCommandSender {

  val logger: Logger = LoggerFactory.getLogger(TaskCommandSender::class.java)

  @EventListener(condition = "#command.enriched")
  override fun sendTaskCommand(command: AssignTaskCommand) {
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
  override fun sendTaskCommand(command: CreateTaskCommand) {
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
  override fun sendTaskCommand(command: CompleteTaskCommand) {
    send(command)
  }

  @EventListener(condition = "#command.enriched")
  override fun sendTaskCommand(command: DeleteTaskCommand) {
    send(command)
  }

  /**
   * Update task commands are sent without being enriched.
   */
  @EventListener
  override fun sendTaskCommand(command: UpdateTaskCommand) {
    send(command)
  }


  private fun send(command: Any) {
    if (properties.sender.enabled) {
      gateway.send<Any, Any?>(command) { m, r -> logger.info("Successfully submitted command $m, $r") }
    } else {
      logger.debug("Would have sent command $command")
    }
  }

}
