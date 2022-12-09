package io.holunda.polyflow.taskpool.sender

import com.fasterxml.jackson.databind.ObjectMapper
import io.holunda.camunda.taskpool.api.task.EngineTaskCommand
import io.holunda.polyflow.taskpool.sender.gateway.CommandListGateway
import mu.KLogging
import org.camunda.bpm.engine.impl.interceptor.CommandContext
import org.camunda.bpm.engine.impl.jobexecutor.JobHandler
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity
import org.camunda.bpm.engine.impl.persistence.entity.JobEntity
import org.camunda.bpm.engine.impl.persistence.entity.ResourceEntity

class EngineTaskCommandsSendingJobHandler(
  private val objectMapper: ObjectMapper,
  private val commandListGateway: CommandListGateway
) : JobHandler<EngineTaskCommandsSendingJobHandlerConfiguration> {

  companion object : KLogging() {
    const val TYPE = "polyflow-engine-task-command-sending"
  }

  private val engineTaskCommandListType = objectMapper.typeFactory.constructCollectionLikeType(List::class.java, EngineTaskCommand::class.java)

  override fun getType(): String = TYPE

  override fun newConfiguration(canonicalString: String): EngineTaskCommandsSendingJobHandlerConfiguration =
    EngineTaskCommandsSendingJobHandlerConfiguration.fromCanonicalString(value = canonicalString, objectMapper = objectMapper)

  override fun onDelete(configuration: EngineTaskCommandsSendingJobHandlerConfiguration, jobEntity: JobEntity) {
  }

  override fun execute(
    configuration: EngineTaskCommandsSendingJobHandlerConfiguration,
    execution: ExecutionEntity?,
    commandContext: CommandContext,
    tenantId: String?
  ) {
    val commands = commandContext.readCommands(configuration.commandByteArrayId)
    commandListGateway.sendToGateway(commands)
    commandContext.deleteCommands(configuration.commandByteArrayId)
  }

  private fun CommandContext.readCommands(commandByteArrayId: String): List<EngineTaskCommand> {
    val resourceEntity = this.dbEntityManager.selectOne("selectResourceById", commandByteArrayId) as ResourceEntity
    return objectMapper.readValue(resourceEntity.bytes, engineTaskCommandListType)
  }

  private fun CommandContext.deleteCommands(commandByteArrayId: String) =
    this.byteArrayManager.deleteByteArrayById(commandByteArrayId)

}