package io.holunda.camunda.taskpool.sender

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.holunda.camunda.taskpool.sender.gateway.*
import io.holunda.camunda.taskpool.sender.process.definition.ProcessDefinitionCommandSender
import io.holunda.camunda.taskpool.sender.process.definition.SimpleProcessDefinitionCommandSender
import io.holunda.camunda.taskpool.sender.process.instance.ProcessInstanceCommandSender
import io.holunda.camunda.taskpool.sender.process.instance.SimpleProcessInstanceCommandSender
import io.holunda.camunda.taskpool.sender.process.variable.ProcessVariableCommandSender
import io.holunda.camunda.taskpool.sender.process.variable.SimpleProcessVariableCommandSender
import io.holunda.camunda.taskpool.sender.task.EngineTaskCommandSender
import io.holunda.camunda.taskpool.sender.task.SimpleEngineTaskCommandSender
import io.holunda.camunda.taskpool.sender.task.TxAwareAccumulatingEngineTaskCommandSender
import io.holunda.camunda.taskpool.sender.task.accumulator.EngineTaskCommandAccumulator
import io.holunda.camunda.taskpool.sender.task.accumulator.ProjectingCommandAccumulator
import org.axonframework.commandhandling.gateway.CommandGateway
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import javax.annotation.PostConstruct

@Configuration
@ComponentScan
@EnableConfigurationProperties(SenderProperties::class)
class SenderConfiguration(private val senderProperties: SenderProperties) {

  private val logger: Logger = LoggerFactory.getLogger(SenderConfiguration::class.java)
  /**
   * Conditional object mapper, if not defined by the user.
   */
  @Bean
  @ConditionalOnMissingBean(ObjectMapper::class)
  fun taskCollectorObjectMapper(): ObjectMapper = jacksonObjectMapper().findAndRegisterModules()

  /**
   * Create task accumulator.
   */
  @Bean
  fun taskCommandAccumulator(objectMapper: ObjectMapper): EngineTaskCommandAccumulator = ProjectingCommandAccumulator(objectMapper = objectMapper)

  /**
   * Create a command list gateway, if none is provided.
   */
  @Bean
  @ConditionalOnMissingBean(CommandListGateway::class)
  fun defaultCommandListGateway(
    commandGateway: CommandGateway,
    senderProperties: SenderProperties,
    commandSuccessHandler: CommandSuccessHandler,
    commandErrorHandler: CommandErrorHandler
  ): CommandListGateway
    = AxonCommandListGateway(
    commandGateway = commandGateway,
    senderProperties = senderProperties,
    commandSuccessHandler = commandSuccessHandler,
    commandErrorHandler = commandErrorHandler
  )

  /**
   * Create command sender for tasks.
   */
  @Bean
  @ConditionalOnExpression("'\${polyflow.integration.sender.task.type}' != 'custom'")
  fun taskCommandSender(commandListGateway: CommandListGateway, accumulator: EngineTaskCommandAccumulator): EngineTaskCommandSender =
    when (senderProperties.task.type) {
      SenderType.tx -> TxAwareAccumulatingEngineTaskCommandSender(commandListGateway, accumulator, senderProperties)
      SenderType.simple -> SimpleEngineTaskCommandSender(commandListGateway, senderProperties)
      else -> throw IllegalStateException("Could not initialize sender, used unknown ${senderProperties.task.type} type.")
    }

  /**
   * Create command sender for process definitions.
   */
  @Bean
  @ConditionalOnExpression("'\${polyflow.integration.sender.process-definition.type}' != 'custom'")
  fun processDefinitionCommandSender(commandListGateway: CommandListGateway): ProcessDefinitionCommandSender =
    when (senderProperties.processDefinition.type) {
      SenderType.simple -> SimpleProcessDefinitionCommandSender(commandListGateway, senderProperties)
      else -> throw IllegalStateException("Could not initialize sender, used unknown ${senderProperties.processDefinition.type} type.")
    }

  /**
   * Create command sender for process instances.
   */
  @Bean
  @ConditionalOnExpression("'\${polyflow.integration.sender.process-instance.type}' != 'custom'")
  fun processInstanceCommandSender(commandListGateway: CommandListGateway): ProcessInstanceCommandSender =
    when (senderProperties.processInstance.type) {
      SenderType.simple -> SimpleProcessInstanceCommandSender(commandListGateway, senderProperties)
      else -> throw IllegalStateException("Could not initialize sender, used unknown ${senderProperties.processInstance.type} type.")
    }

  /**
   * Create command sender for process instances.
   */
  @Bean
  @ConditionalOnExpression("'\${polyflow.integration.sender.process-variable.type}' != 'custom'")
  fun processVariableCommandSender(commandListGateway: CommandListGateway, objectMapper: ObjectMapper): ProcessVariableCommandSender =
    when (senderProperties.processVariable.type) {
      SenderType.simple -> SimpleProcessVariableCommandSender(commandListGateway, senderProperties, objectMapper)
      else -> throw IllegalStateException("Could not initialize sender, used unknown ${senderProperties.processVariable.type} type.")
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
