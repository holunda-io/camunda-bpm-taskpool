package io.holunda.polyflow.taskpool.sender

import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.holunda.polyflow.bus.jackson.annotation.ConditionalOnMissingQualifiedBean
import io.holunda.polyflow.bus.jackson.configurePolyflowJacksonObjectMapper
import io.holunda.polyflow.taskpool.sender.gateway.CommandListGateway
import io.holunda.polyflow.taskpool.sender.task.EngineTaskCommandSender
import io.holunda.polyflow.taskpool.sender.task.accumulator.EngineTaskCommandAccumulator
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl
import org.camunda.bpm.engine.spring.SpringProcessEnginePlugin
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression
import org.springframework.context.annotation.Bean

/**
 * Spring configuration building task sender, using Camunda job to decouple from originated transaction..
 */
class CamundaJobSenderConfiguration(
  private val senderProperties: SenderProperties
) {

  companion object {
    const val COMMAND_BYTEARRAY_OBJECT_MAPPER = "commandByteArrayObjectMapper"
  }

  /**
   * Creates transactional command sender for tasks.
   */
  @Bean
  @ConditionalOnExpression("'\${polyflow.integration.sender.task.type}' == 'txjob'")
  fun camundaJobTaskCommandSender(
    processEngineConfiguration: ProcessEngineConfigurationImpl,
    @Qualifier(COMMAND_BYTEARRAY_OBJECT_MAPPER)
    objectMapper: ObjectMapper,
    commandListGateway: CommandListGateway,
    engineTaskCommandAccumulator: EngineTaskCommandAccumulator
  ): EngineTaskCommandSender =
    TxAwareAccumulatingCamundaJobEngineTaskCommandSender(
      processEngineConfiguration = processEngineConfiguration,
      objectMapper = objectMapper,
      senderProperties = senderProperties,
      engineTaskCommandAccumulator = engineTaskCommandAccumulator
    )

  /**
   * Build the engine plugin to install pre-built listeners.
   */
  @Bean
  fun camundaEngineTaskCommandSendingJobHandlerEnginePlugin(
    @Qualifier(COMMAND_BYTEARRAY_OBJECT_MAPPER)
    objectMapper: ObjectMapper,
    commandListGateway: CommandListGateway
  ) = object : SpringProcessEnginePlugin() {
    override fun preInit(processEngineConfiguration: ProcessEngineConfigurationImpl) {
      processEngineConfiguration.customJobHandlers =
        (processEngineConfiguration.customJobHandlers ?: mutableListOf()) + EngineTaskCommandsSendingJobHandler(
          objectMapper = objectMapper,
          commandListGateway = commandListGateway
        )
    }
  }

  @Bean
  @Qualifier(COMMAND_BYTEARRAY_OBJECT_MAPPER)
  @ConditionalOnMissingQualifiedBean(beanClass = ObjectMapper::class, qualifier = COMMAND_BYTEARRAY_OBJECT_MAPPER)
  fun fallbackCommandByteArrayObjectMapper(): ObjectMapper =
    jacksonObjectMapper()
      .configurePolyflowJacksonObjectMapper()
      .apply {
        activateDefaultTyping(LaissezFaireSubTypeValidator(), ObjectMapper.DefaultTyping.EVERYTHING, JsonTypeInfo.As.WRAPPER_ARRAY)
      }
}