package io.holunda.polyflow.datapool

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.oshai.kotlinlogging.KotlinLogging
import io.holunda.polyflow.datapool.projector.DataEntryProjectionSupplier
import io.holunda.polyflow.datapool.projector.DataEntryProjector
import io.holunda.polyflow.datapool.sender.AbstractDataEntryCommandSender
import io.holunda.polyflow.datapool.sender.DirectTxAwareAccumulatingDataEntryCommandSender
import io.holunda.polyflow.datapool.sender.SimpleDataEntryCommandSender
import io.holunda.polyflow.datapool.sender.gateway.*
import io.holunda.polyflow.spring.ApplicationNameBeanPostProcessor
import jakarta.annotation.PostConstruct
import org.axonframework.commandhandling.gateway.CommandGateway
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import

/**
 * Polyflow sender configuration.
 */
@EnableConfigurationProperties(DataPoolSenderProperties::class)
@Import(ApplicationNameBeanPostProcessor::class)
class DataEntrySenderConfiguration(
  private val senderProperties: DataPoolSenderProperties
) {

  /** Logger instance for this class. */
  private val logger = KotlinLogging.logger {}

  /**
   * Initializes the projector.
   */
  @Bean
  fun dataEntryProjector(suppliers: List<DataEntryProjectionSupplier>) = DataEntryProjector(suppliers)

  /**
   * Default handler.
   */
  @Bean
  fun loggingDataEntryCommandSuccessHandler(): CommandSuccessHandler = LoggingDataEntryCommandSuccessHandler(logger)

  /**
   * Default handler.
   */
  @Bean
  fun loggingDataEntryCommandErrorHandler(): CommandErrorHandler = LoggingDataEntryCommandErrorHandler(logger)

  /**
   * Creates a command list gateway, if none is provided.
   */
  @Bean
  @ConditionalOnMissingBean(CommandListGateway::class)
  fun dataEntryCommandListGateway(
    commandGateway: CommandGateway,
    commandSuccessHandler: CommandSuccessHandler,
    commandErrorHandler: CommandErrorHandler
  ) = AxonCommandListGateway(
    commandGateway,
    senderProperties,
    commandSuccessHandler,
    commandErrorHandler
  )

  /**
   * Provide data entry sender properties as injectable Spring bean.
   */
  @Bean
  fun dataEntrySenderProperties() = senderProperties.dataEntry

  /**
   * Creates simple (direct) command sender for data entries.
   */
  @ConditionalOnProperty(value = ["polyflow.integration.sender.data-entry.type"], havingValue = "simple", matchIfMissing = true)
  @Bean
  fun simpleDataEntryCommandSender(
    commandListGateway: CommandListGateway,
    dataEntryProjector: DataEntryProjector,
    objectMapper: ObjectMapper
  ): AbstractDataEntryCommandSender = SimpleDataEntryCommandSender(
    commandListGateway,
    senderProperties.dataEntry,
    dataEntryProjector,
    objectMapper
  )

  /**
   * Creates transactional (direct) command sender for data entries.
   */
  @Bean
  @ConditionalOnProperty(value = ["polyflow.integration.sender.data-entry.type"], havingValue = "tx", matchIfMissing = false)
  fun txAwareDirectDataEntryCommandSender(
    commandListGateway: CommandListGateway,
    dataEntryProjector: DataEntryProjector,
    objectMapper: ObjectMapper
  ): AbstractDataEntryCommandSender = DirectTxAwareAccumulatingDataEntryCommandSender(
    commandListGateway,
    senderProperties.dataEntry,
    dataEntryProjector,
    objectMapper
  )

  /**
   * Prints sender config.
   */
  @PostConstruct
  fun printSenderConfiguration() {
    if (senderProperties.dataEntry.enabled) {
      logger.info{ "SENDER-111: Datapool data entry commands will be distributed over command bus." }
    } else {
      logger.info{ "SENDER-112: Datapool data entry command distribution is disabled by property." }
    }
  }
}
