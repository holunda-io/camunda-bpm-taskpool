package io.holunda.camunda.taskpool.collector.process.definition

import io.holunda.camunda.taskpool.sender.process.definition.ProcessDefinitionCommandSender
import mu.KLogging
import org.camunda.bpm.engine.impl.interceptor.Command
import org.camunda.bpm.engine.impl.interceptor.CommandContext
import org.camunda.bpm.engine.impl.jobexecutor.JobHandler
import org.camunda.bpm.engine.impl.jobexecutor.JobHandlerConfiguration
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity
import org.camunda.bpm.engine.impl.persistence.entity.JobEntity
import org.camunda.bpm.engine.impl.persistence.entity.MessageEntity
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component

/**
 * Sends out commands containing information about deployed process definitions.
 * <p>This bean is instantiated by Camunda and is intentionally using LAZY field injection,
 * in order to avoid dependency cycles.
 * </p>
 */
@Component
class RefreshProcessDefinitionsJobHandler(
  private val processDefinitionService: ProcessDefinitionService,
  @Lazy
  private val processDefinitionCommandSender: ProcessDefinitionCommandSender
) : JobHandler<RefreshProcessDefinitionsJobConfiguration> {

  companion object : KLogging() {
    const val TYPE = "RefreshProcessDefinitionsJobHandler"
  }

  override fun execute(configuration: RefreshProcessDefinitionsJobConfiguration, execution: ExecutionEntity?, commandContext: CommandContext, tenantId: String?) {

    // deliver new commands on deployment of processes only.
    val commands = processDefinitionService.getProcessDefinitions(
      formService = commandContext.processEngineConfiguration.formService,
      repositoryService = commandContext.processEngineConfiguration.repositoryService,
      processDefinitionKey = configuration.processDefinitionKey,
      returnAll = false
    )
    // send to the task pool core.
    logger.info { "EVENTING-022: Registering ${commands.size} new process definitions." }
    commands.forEach {
      processDefinitionCommandSender.send(it)
    }
  }


  override fun newConfiguration(canonicalString: String) = RefreshProcessDefinitionsJobConfiguration(canonicalString)
  override fun onDelete(configuration: RefreshProcessDefinitionsJobConfiguration, jobEntity: JobEntity) {}
  override fun getType(): String = TYPE
}

/**
 * Command to inform about new process definition.
 */
data class RefreshProcessDefinitionsJobCommand(val processDefinitionKey: String) : Command<String> {

  companion object : KLogging()

  override fun execute(commandContext: CommandContext): String {
    logger.info { "EVENTING-021: New process definition detected. Sending the command for ${this.processDefinitionKey}." }
    val message = MessageEntity()
    message.init(commandContext)
    message.jobHandlerType = RefreshProcessDefinitionsJobHandler.TYPE
    message.jobHandlerConfiguration = RefreshProcessDefinitionsJobConfiguration(this.processDefinitionKey)
    commandContext.jobManager.send(message)

    return message.id
  }
}

/**
 * Storage of job configuration.
 */
data class RefreshProcessDefinitionsJobConfiguration(val processDefinitionKey: String) : JobHandlerConfiguration {
  override fun toCanonicalString(): String = processDefinitionKey
}

