package io.holunda.polyflow.taskpool.sender

import com.fasterxml.jackson.annotation.JsonTypeInfo
import tools.jackson.databind.DefaultTyping
import tools.jackson.databind.ObjectMapper
import tools.jackson.databind.json.JsonMapper
import tools.jackson.databind.jsontype.BasicPolymorphicTypeValidator
import tools.jackson.module.kotlin.jacksonObjectMapper
import io.holunda.polyflow.bus.jackson.configurePolyflowJacksonObjectMapper
import io.holunda.polyflow.taskpool.sender.gateway.CommandListGateway
import io.holunda.polyflow.taskpool.sender.task.EngineTaskCommandSender
import io.holunda.polyflow.taskpool.sender.task.accumulator.EngineTaskCommandAccumulator
import io.toolisticon.spring.condition.ConditionalOnMissingQualifiedBean
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl
import org.camunda.bpm.engine.spring.SpringProcessEnginePlugin
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * Spring configuration building task sender, using Camunda job to decouple from originated transaction..
 */
@Configuration
class CamundaJobSenderConfiguration(
  private val senderProperties: SenderProperties
) {

  /**
   * Name of the bean.
   */
  companion object {
    const val COMMAND_BYTEARRAY_OBJECT_MAPPER = "commandByteArrayObjectMapper"
  }

  /**
   * Creates transactional command sender for tasks.
   */
  @Bean
  @ConditionalOnProperty(value = ["polyflow.integration.sender.task.type"], havingValue = "txjob")
  fun camundaJobTaskCommandSender(
    processEngineConfiguration: ProcessEngineConfigurationImpl,
    @Qualifier(COMMAND_BYTEARRAY_OBJECT_MAPPER)
    objectMapper: ObjectMapper,
    engineTaskCommandAccumulator: EngineTaskCommandAccumulator
  ): EngineTaskCommandSender =
    TxAwareAccumulatingCamundaJobEngineTaskCommandSender(
      processEngineConfiguration = processEngineConfiguration,
      objectMapper = objectMapper,
      senderProperties = senderProperties,
      engineTaskCommandAccumulator = engineTaskCommandAccumulator
    )

  /**
   * Build the engine plugin to install the job handler.
   */
  @Bean
  @ConditionalOnProperty(value = ["polyflow.integration.sender.task.type"], havingValue = "txjob")
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

  /**
   * Object mapper for serializing and deserializing commands to camunda bytearray and back.
   */
  @Bean
  @Qualifier(COMMAND_BYTEARRAY_OBJECT_MAPPER)
  @ConditionalOnMissingQualifiedBean(beanClass = ObjectMapper::class, qualifier = COMMAND_BYTEARRAY_OBJECT_MAPPER)
  fun fallbackCommandByteArrayObjectMapper(): ObjectMapper =
    jacksonObjectMapper()
      .configurePolyflowJacksonObjectMapper()
      .rebuild<JsonMapper, JsonMapper.Builder>()
      .activateDefaultTyping(
        BasicPolymorphicTypeValidator.builder()
          .allowIfSubType(Any::class.java)
          .allowIfSubTypeIsArray()
          .build(),
        DefaultTyping.NON_FINAL_AND_ENUMS,
        JsonTypeInfo.As.WRAPPER_ARRAY
      )
      .build()
}
