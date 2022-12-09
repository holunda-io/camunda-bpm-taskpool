package io.holunda.polyflow.taskpool.sender

import com.fasterxml.jackson.databind.ObjectMapper
import io.holunda.polyflow.bus.jackson.config.FallbackPayloadObjectMapperAutoConfiguration.Companion.PAYLOAD_OBJECT_MAPPER
import io.holunda.polyflow.taskpool.sender.gateway.*
import io.holunda.polyflow.taskpool.sender.process.definition.ProcessDefinitionCommandSender
import io.holunda.polyflow.taskpool.sender.process.definition.SimpleProcessDefinitionCommandSender
import io.holunda.polyflow.taskpool.sender.process.instance.ProcessInstanceCommandSender
import io.holunda.polyflow.taskpool.sender.process.instance.SimpleProcessInstanceCommandSender
import io.holunda.polyflow.taskpool.sender.process.variable.ProcessVariableCommandSender
import io.holunda.polyflow.taskpool.sender.process.variable.SimpleProcessVariableCommandSender
import io.holunda.polyflow.taskpool.sender.process.variable.TxAwareAccumulatingProcessVariableCommandSender
import io.holunda.polyflow.taskpool.sender.task.DirectTxAwareAccumulatingEngineTaskCommandSender
import io.holunda.polyflow.taskpool.sender.task.EngineTaskCommandSender
import io.holunda.polyflow.taskpool.sender.task.SimpleEngineTaskCommandSender
import io.holunda.polyflow.taskpool.sender.task.TxAwareAccumulatingEngineTaskCommandSender
import io.holunda.polyflow.taskpool.sender.task.accumulator.EngineTaskCommandAccumulator
import io.holunda.polyflow.taskpool.sender.task.accumulator.ProjectingCommandAccumulator
import org.axonframework.commandhandling.gateway.CommandGateway
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.annotation.Bean
import javax.annotation.PostConstruct

/**
 * Main configuration of the taskpool sender component.
 */
@EnableConfigurationProperties(SenderProperties::class)
class SenderConfiguration(private val senderProperties: SenderProperties) {

  private val logger: Logger = LoggerFactory.getLogger(SenderConfiguration::class.java)

  /**
   * Creates generic task publisher.
   */
  @Bean
  fun genericTaskPublisher(applicationEventPublisher: ApplicationEventPublisher) =
    GenericTaskPublisher(applicationEventPublisher = applicationEventPublisher)

  /**
   * Creates task accumulator.
   */
  @Bean
  fun taskCommandAccumulator(@Qualifier(PAYLOAD_OBJECT_MAPPER) objectMapper: ObjectMapper): EngineTaskCommandAccumulator =
    ProjectingCommandAccumulator(
      objectMapper = objectMapper,
      serializePayload = senderProperties.task.serializePayload,
      simpleIntentDetectionBehaviour = false
    )

  /**
   * Creates a command list gateway, if none is provided.
   */
  @Bean
  @ConditionalOnMissingBean(CommandListGateway::class)
  fun defaultCommandListGateway(
    commandGateway: CommandGateway,
    senderProperties: SenderProperties,
    commandSuccessHandler: CommandSuccessHandler,
    commandErrorHandler: CommandErrorHandler
  ): CommandListGateway = AxonCommandListGateway(
    commandGateway = commandGateway,
    senderProperties = senderProperties,
    commandSuccessHandler = commandSuccessHandler,
    commandErrorHandler = commandErrorHandler
  )

  /**
   * Creates simple (direct) command sender for tasks.
   */
  @Bean
  @ConditionalOnExpression("'\${polyflow.integration.sender.task.type}' == 'simple'")
  fun simpleTaskCommandSender(commandListGateway: CommandListGateway, accumulator: EngineTaskCommandAccumulator): EngineTaskCommandSender =
    SimpleEngineTaskCommandSender(commandListGateway, senderProperties)

  /**
   * Creates transactional command sender for tasks.
   */
  @Bean
  @ConditionalOnExpression("'\${polyflow.integration.sender.task.type}' == 'tx'")
  fun txAwareTaskCommandSender(commandListGateway: CommandListGateway, accumulator: EngineTaskCommandAccumulator): EngineTaskCommandSender =
    DirectTxAwareAccumulatingEngineTaskCommandSender(commandListGateway, accumulator, senderProperties)


  /**
   * Creates command sender for process definitions.
   */
  @Bean
  @ConditionalOnExpression("'\${polyflow.integration.sender.process-definition.type}' != 'custom'")
  fun processDefinitionCommandSender(commandListGateway: CommandListGateway): ProcessDefinitionCommandSender =
    when (senderProperties.processDefinition.type) {
      SenderType.simple -> SimpleProcessDefinitionCommandSender(commandListGateway, senderProperties)
      else -> throw IllegalStateException("Could not initialize definition sender, used unknown ${senderProperties.processDefinition.type} type.")
    }

  /**
   * Creates command sender for process instances.
   */
  @Bean
  @ConditionalOnExpression("'\${polyflow.integration.sender.process-instance.type}' != 'custom'")
  fun processInstanceCommandSender(commandListGateway: CommandListGateway): ProcessInstanceCommandSender =
    when (senderProperties.processInstance.type) {
      SenderType.simple -> SimpleProcessInstanceCommandSender(commandListGateway, senderProperties)
      else -> throw IllegalStateException("Could not initialize instance sender, used unknown ${senderProperties.processInstance.type} type.")
    }

  /**
   * Creates command sender for process instances.
   */
  @Bean
  @ConditionalOnExpression("'\${polyflow.integration.sender.process-variable.type}' != 'custom'")
  fun processVariableCommandSender(
    commandListGateway: CommandListGateway,
    @Qualifier(PAYLOAD_OBJECT_MAPPER) objectMapper: ObjectMapper
  ): ProcessVariableCommandSender =
    when (senderProperties.processVariable.type) {
      SenderType.simple -> SimpleProcessVariableCommandSender(commandListGateway, senderProperties, objectMapper)
      SenderType.tx -> TxAwareAccumulatingProcessVariableCommandSender(commandListGateway, senderProperties, objectMapper)
      else -> throw IllegalStateException("Could not initialize variable sender, used unknown ${senderProperties.processVariable.type} type.")
    }

  /**
   * Prints sender config.
   */
  @PostConstruct
  fun printSenderConfiguration() {
    if (senderProperties.task.enabled) {
      logger.info("SENDER-011: Taskpool task commands will be distributed over command bus.")
    } else {
      logger.info("SENDER-012: Taskpool task command distribution is disabled by property.")
    }
    if (senderProperties.processDefinition.enabled) {
      logger.info("SENDER-013: Taskpool process definition commands will be distributed over command bus.")
    } else {
      logger.info("SENDER-014: Taskpool process definition command distribution is disabled by property.")
    }
    if (senderProperties.processInstance.enabled) {
      logger.info("SENDER-015: Taskpool process instance commands will be distributed over command bus.")
    } else {
      logger.info("SENDER-016: Taskpool process instance command distribution is disabled by property.")
    }
    if (senderProperties.processVariable.enabled) {
      logger.info("SENDER-017: Taskpool process variable commands will be distributed over command bus.")
    } else {
      logger.info("SENDER-018: Taskpool process variable command distribution is disabled by property.")
    }
  }

  /**
   * Default logging handler.
   */
  @Bean
  @ConditionalOnMissingBean(CommandSuccessHandler::class)
  fun loggingTaskCommandSuccessHandler(): CommandSuccessHandler = LoggingTaskCommandSuccessHandler(logger)

  /**
   * Default logging handler.
   */
  @Bean
  @ConditionalOnMissingBean(CommandErrorHandler::class)
  fun loggingTaskCommandErrorHandler(): CommandErrorHandler = LoggingTaskCommandErrorHandler(logger)

}
