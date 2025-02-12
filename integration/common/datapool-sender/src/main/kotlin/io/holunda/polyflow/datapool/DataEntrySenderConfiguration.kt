package io.holunda.polyflow.datapool

import com.fasterxml.jackson.databind.ObjectMapper
import io.holunda.polyflow.bus.jackson.config.FallbackPayloadObjectMapperAutoConfiguration.Companion.PAYLOAD_OBJECT_MAPPER
import io.holunda.polyflow.datapool.projector.DataEntryProjectionSupplier
import io.holunda.polyflow.datapool.projector.DataEntryProjector
import io.holunda.polyflow.datapool.sender.*
import io.holunda.polyflow.datapool.sender.gateway.*
import io.holunda.polyflow.spring.ApplicationNameBeanPostProcessor
import org.axonframework.commandhandling.gateway.CommandGateway
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import

/**
 * Polyflow sender configuration.
 */
@EnableConfigurationProperties(DataEntrySenderProperties::class)
@Import(ApplicationNameBeanPostProcessor::class)
class DataEntrySenderConfiguration(
  val properties: DataEntrySenderProperties
) {

  /**
   * Initializes the projector.
   */
  @Bean
  fun dataEntryProjector(suppliers: List<DataEntryProjectionSupplier>) = DataEntryProjector(suppliers)

  /**
   * Default handler.
   */
  @Bean
  fun loggingDataEntryCommandSuccessHandler(): CommandSuccessHandler =
    LoggingDataEntryCommandSuccessHandler(LoggerFactory.getLogger(DataEntryCommandSender::class.java))

  /**
   * Default handler.
   */
  @Bean
  fun loggingDataEntryCommandErrorHandler(): CommandErrorHandler =
    LoggingDataEntryCommandErrorHandler(LoggerFactory.getLogger(DataEntryCommandSender::class.java))

  @Bean
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
  ) = SimpleDataEntrySender(
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

}
