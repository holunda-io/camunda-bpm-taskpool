package io.holunda.polyflow.datapool

import com.fasterxml.jackson.databind.ObjectMapper
import io.holunda.polyflow.bus.jackson.config.FallbackPayloadObjectMapperAutoConfiguration.Companion.PAYLOAD_OBJECT_MAPPER
import io.holunda.polyflow.datapool.projector.DataEntryProjectionSupplier
import io.holunda.polyflow.datapool.projector.DataEntryProjector
import io.holunda.polyflow.datapool.sender.*
import org.axonframework.commandhandling.gateway.CommandGateway
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * Polyflow sender configuration.
 */
@EnableConfigurationProperties(DataEntrySenderProperties::class)
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
  fun loggingDataEntryCommandSuccessHandler(): DataEntryCommandSuccessHandler =
    LoggingCommandSuccessHandler(LoggerFactory.getLogger(DataEntryCommandSender::class.java))

  /**
   * Default handler.
   */
  @Bean
  fun loggingDataEntryCommandErrorHandler(): DataEntryCommandErrorHandler =
    LoggingCommandErrorHandler(LoggerFactory.getLogger(DataEntryCommandSender::class.java))

  /**
   * Default configuration of the data entry sender.
   */
  @Bean
  @ConditionalOnExpression("'\${polyflow.integration.sender.data-entry.type}' != 'custom'")
  fun configureSender(
    gateway: CommandGateway,
    dataEntryProjector: DataEntryProjector,
    dataEntryCommandSuccessHandler: DataEntryCommandSuccessHandler,
    dataEntryCommandErrorHandler: DataEntryCommandErrorHandler,
    @Qualifier(PAYLOAD_OBJECT_MAPPER)
    objectMapper: ObjectMapper,
  ): DataEntryCommandSender {
    return when (properties.type) {
      DataEntrySenderType.simple -> SimpleDataEntryCommandSender(
        gateway = gateway,
        properties = properties,
        dataEntryProjector = dataEntryProjector,
        successHandler = dataEntryCommandSuccessHandler,
        errorHandler = dataEntryCommandErrorHandler,
        objectMapper = objectMapper
      )
      else -> throw IllegalStateException("Could not initialize sender, used unknown ${properties.type} type.")
    }
  }
}
