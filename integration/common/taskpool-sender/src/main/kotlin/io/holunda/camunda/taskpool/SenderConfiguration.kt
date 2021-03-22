package io.holunda.camunda.taskpool

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.holunda.camunda.taskpool.sender.gateway.*
import io.holunda.camunda.taskpool.sender.process.definition.ProcessDefinitionCommandSender
import io.holunda.camunda.taskpool.sender.process.definition.SimpleProcessDefinitionCommandSender
import io.holunda.camunda.taskpool.sender.process.instance.ProcessInstanceCommandSender
import io.holunda.camunda.taskpool.sender.process.instance.SimpleProcessInstanceCommandSender
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
class SenderConfiguration(private val properties: SenderProperties) {

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
  @ConditionalOnBean(CommandGateway::class)
  @ConditionalOnMissingBean(CommandListGateway::class)
  fun defaultCommandListGateway(
    commandGateway: CommandGateway,
    senderProperties: SenderProperties,
    commandSuccessHandler: CommandSuccessHandler,
    commandErrorHandler: CommandErrorHandler
  ): CommandListGateway
    = AxonCommandListGateway(
    commandGateway = commandGateway,
    properties = senderProperties,
    commandSuccessHandler = commandSuccessHandler,
    commandErrorHandler = commandErrorHandler
  )

  /**
   * Create command sender for tasks.
   */
  @Bean
  @ConditionalOnExpression("'\${polyflow.integration.sender.task.type}' != 'custom'")
  fun taskCommandSender(commandListGateway: CommandListGateway, accumulator: EngineTaskCommandAccumulator): EngineTaskCommandSender =
    when (properties.task.type) {
      SenderType.tx -> TxAwareAccumulatingEngineTaskCommandSender(commandListGateway, accumulator, properties.task.sendWithinTransaction)
      SenderType.simple -> SimpleEngineTaskCommandSender(commandListGateway)
      else -> throw IllegalStateException("Could not initialize sender, used unknown ${properties.task.type} type.")
    }

  /**
   * Create command sender for process definitions.
   */
  @Bean
  @ConditionalOnExpression("'\${polyflow.integration.sender.process-definition.type}' != 'custom'")
  fun processDefinitionCommandSender(commandListGateway: CommandListGateway): ProcessDefinitionCommandSender =
    when (properties.processDefinition.type) {
      SenderType.simple -> SimpleProcessDefinitionCommandSender(commandListGateway)
      else -> throw IllegalStateException("Could not initialize sender, used unknown ${properties.processDefinition.type} type.")
    }

  /**
   * Create command sender for process instances.
   */
  @Bean
  @ConditionalOnExpression("'\${polyflow.integration.sender.process-instance.type}' != 'custom'")
  fun processInstanceCommandSender(commandListGateway: CommandListGateway): ProcessInstanceCommandSender =
    when (properties.processInstance.type) {
      SenderType.simple -> SimpleProcessInstanceCommandSender(commandListGateway)
      else -> throw IllegalStateException("Could not initialize sender, used unknown ${properties.processInstance.type} type.")
    }

  /**
   * Prints sender config.
   */
  @PostConstruct
  fun printSenderConfiguration() {
    if (properties.task.enabled) {
      logger.info("SENDER-011: Taskpool task commands will be distributed over command bus.")
    } else {
      logger.info("SENDER-012: Taskpool task command distribution is disabled by property.")
    }
    if (properties.processDefinition.enabled) {
      logger.info("SENDER-013: Taskpool process definition commands will be distributed over command bus.")
    } else {
      logger.info("SENDER-014: Taskpool process definition command distribution is disabled by property.")
    }
    if (properties.processInstance.enabled) {
      logger.info("SENDER-015: Taskpool process instance commands will be distributed over command bus.")
    } else {
      logger.info("SENDER-016: Taskpool process instance command distribution is disabled by property.")
    }
  }

  /**
   * Default logging handler.
   */
  @Bean
  @ConditionalOnMissingBean(CommandSuccessHandler::class)
  fun loggingTaskCommandSuccessHandler(): CommandSuccessHandler = LoggingTaskCommandSuccessHandler(LoggerFactory.getLogger(EngineTaskCommandSender::class.java))

  /**
   * Default logging handler.
   */
  @Bean
  @ConditionalOnMissingBean(CommandErrorHandler::class)
  fun loggingTaskCommandErrorHandler(): CommandErrorHandler = LoggingTaskCommandErrorHandler(LoggerFactory.getLogger(EngineTaskCommandSender::class.java))


}
