package io.holunda.polyflow.datapool

import com.fasterxml.jackson.databind.ObjectMapper
import io.holunda.polyflow.bus.jackson.config.FallbackPayloadObjectMapperAutoConfiguration.Companion.PAYLOAD_OBJECT_MAPPER
import io.holunda.polyflow.datapool.projector.DataEntryProjectionSupplier
import io.holunda.polyflow.datapool.projector.DataEntryProjector
import io.holunda.polyflow.datapool.sender.*
import io.holunda.polyflow.datapool.sender.gateway.*
import io.holunda.polyflow.spring.ApplicationNameBeanPostProcessor
import io.holunda.polyflow.view.DataEntry
import jakarta.annotation.PostConstruct
import mu.KLogging
import org.axonframework.commandhandling.gateway.CommandGateway
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import javax.xml.crypto.Data

/**
 * Polyflow sender configuration.
 */
@EnableConfigurationProperties(DataEntrySenderProperties::class)
@Import(ApplicationNameBeanPostProcessor::class)
class DataEntrySenderConfiguration(
  private val properties: DataEntrySenderProperties
) {

  /** Logger instance for this class. */
  companion object : KLogging()

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
  fun commandListGateway(
    commandGateway: CommandGateway,
    commandSuccessHandler: CommandSuccessHandler,
    commandErrorHandler: CommandErrorHandler
  ) = AxonCommandListGateway(
    commandGateway,
    properties,
    commandSuccessHandler,
    commandErrorHandler
  )

  /**
   * Default data entry command processor wrapping the DataEntryCommands as Axon CommandMessage containing MetaData.
   */
  @Bean
  fun dataEntryCommandProcessor(
    dataEntrySender: DataEntrySender,
    dataEntryProjector: DataEntryProjector,
    @Qualifier(PAYLOAD_OBJECT_MAPPER)
    objectMapper: ObjectMapper,
  ): DataEntryCommandSender = DataEntryCommandProcessor(
    dataEntrySender = dataEntrySender,
    properties = properties,
    dataEntryProjector = dataEntryProjector,
    objectMapper = objectMapper
  )

  /**
   * Creates simple (direct) command sender for data entries.
   */
  @ConditionalOnProperty(value = ["polyflow.integration.sender.data-entry.type"], havingValue = "simple", matchIfMissing = true)
  @Bean
  fun simpleDataEntryCommandSender(
    commandListGateway: CommandListGateway
  ): DataEntrySender = SimpleDataEntrySender(
    commandListGateway,
    properties
  )

  /**
   * Creates transactional (direct) command sender for data entries.
   */
  @Bean
  @ConditionalOnProperty(value = ["polyflow.integration.sender.data-entry.type"], havingValue = "tx", matchIfMissing = false)
  fun txAwareDataEntryCommandSender(
    commandListGateway: CommandListGateway
  ): DataEntrySender =
    DirectTxAwareAccumulatingDataEntryCommandSender(
      commandListGateway = commandListGateway,
      properties
    )

  /**
   * Prints sender config.
   */
  @PostConstruct
  fun printSenderConfiguration() {
    if (properties.enabled) {
      logger.info("SENDER-111: Datapool data entry commands will be distributed over command bus.")
    } else {
      logger.info("SENDER-112: Datakpool data entry command distribution is disabled by property.")
    }
  }
}
