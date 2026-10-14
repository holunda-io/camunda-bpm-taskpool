package io.holunda.polyflow.taskpool.collector.process.definition

import io.holunda.camunda.taskpool.api.process.definition.ActivateProcessDefinitionCommand
import io.holunda.camunda.taskpool.api.process.definition.ProcessDefinitionCommand
import io.holunda.camunda.taskpool.api.process.definition.SuspendProcessDefinitionCommand
import io.holunda.camunda.taskpool.api.process.definition.UnregisterProcessDefinitionCommand
import org.camunda.bpm.engine.impl.cfg.TransactionState
import org.camunda.bpm.engine.impl.context.Context
import org.camunda.bpm.engine.impl.cmd.ActivateProcessDefinitionCmd
import org.camunda.bpm.engine.impl.cmd.DeleteDeploymentCmd
import org.camunda.bpm.engine.impl.cmd.DeleteProcessDefinitionsByIdsCmd
import org.camunda.bpm.engine.impl.cmd.SuspendProcessDefinitionCmd
import org.camunda.bpm.engine.impl.interceptor.Command
import org.camunda.bpm.engine.impl.interceptor.CommandContext
import org.camunda.bpm.engine.impl.interceptor.CommandInterceptor
import org.springframework.context.ApplicationEventPublisher

/**
 * Observes Camunda 7.24 process-definition lifecycle commands and publishes Polyflow commands after commit.
 */
class ProcessDefinitionLifecycleCommandInterceptor(
  private val applicationEventPublisher: ApplicationEventPublisher,
) : CommandInterceptor() {

  /**
   * Captures affected definition identifiers before command execution and publishes lifecycle commands after commit.
   *
   * @param command Camunda command being executed.
   * @param commandContext active Camunda command context.
   * @return command result.
   */
  override fun <T : Any> execute(command: Command<T>): T {
    val commandContext = requireNotNull(Context.getCommandContext()) { "A lifecycle command requires a Camunda command context." }
    val lifecycleCommands = commandsFor(command, commandContext)
    val result = next.execute(command)
    if (lifecycleCommands.isNotEmpty()) {
      commandContext.transactionContext.addTransactionListener(TransactionState.COMMITTED) {
        lifecycleCommands.forEach(applicationEventPublisher::publishEvent)
      }
    }
    return result
  }

  private fun commandsFor(command: Command<*>, context: CommandContext): List<ProcessDefinitionCommand> = when (command) {
    is DeleteDeploymentCmd -> definitionsForDeployment(readField(command, "deploymentId") as String, context).map(::UnregisterProcessDefinitionCommand)
    is DeleteProcessDefinitionsByIdsCmd -> (readField(command, "processDefinitionIds") as Set<*>).filterIsInstance<String>().map(::UnregisterProcessDefinitionCommand)
    is SuspendProcessDefinitionCmd -> affectedDefinitions(command, context).map(::SuspendProcessDefinitionCommand)
    is ActivateProcessDefinitionCmd -> affectedDefinitions(command, context).map(::ActivateProcessDefinitionCommand)
    else -> emptyList()
  }

  private fun definitionsForDeployment(deploymentId: String, context: CommandContext): List<String> =
    context.processDefinitionManager.findProcessDefinitionsByDeploymentId(deploymentId).map { it.id }

  private fun affectedDefinitions(command: Any, context: CommandContext): List<String> {
    val id = readField(command, "processDefinitionId") as String?
    return id?.let(::listOf) ?: context.processDefinitionManager.findDefinitionsByKeyAndTenantId(
      readField(command, "processDefinitionKey") as String,
      readField(command, "tenantId") as String?,
      readField(command, "isTenantIdSet") as Boolean,
    ).map { it.id }
  }

  private fun readField(instance: Any, name: String): Any? {
    var type: Class<*>? = instance.javaClass
    while (type != null) {
      runCatching { type.getDeclaredField(name) }.getOrNull()?.let { field ->
        field.isAccessible = true
        return field.get(instance)
      }
      type = type.superclass
    }
    error("Camunda 7.24 command ${instance.javaClass.name} no longer exposes field $name")
  }
}
