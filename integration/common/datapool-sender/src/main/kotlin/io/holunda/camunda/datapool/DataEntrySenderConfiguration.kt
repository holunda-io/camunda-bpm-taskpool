package io.holunda.camunda.datapool

import com.fasterxml.jackson.databind.ObjectMapper
import io.holunda.camunda.datapool.projector.DataEntryProjector
import io.holunda.camunda.datapool.sender.*
import org.axonframework.commandhandling.gateway.CommandGateway
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration

/**
 * Configuration sender configuration.
 */
@ComponentScan
@Configuration
@EnableConfigurationProperties(DataEntrySenderProperties::class)
class DataEntrySenderConfiguration {

  @Autowired
  lateinit var properties: DataEntrySenderProperties

  /**
   * Default handler.
   */
  @Bean
  fun loggingDataEntryCommandSuccessHandler(): DataEntryCommandSuccessHandler = LoggingCommandSuccessHandler(LoggerFactory.getLogger(DataEntryCommandSender::class.java))

  /**
   * Default handler.
   */
  @Bean
  fun loggingDataEntryCommandErrorHandler(): DataEntryCommandErrorHandler = LoggingCommandErrorHandler(LoggerFactory.getLogger(DataEntryCommandSender::class.java))

  /**
   * Default configuration of the simple sender.
   */
  @Bean
  @ConditionalOnProperty(name = ["camunda.taskpool.dataentry.sender.type"], havingValue = "simple")
  fun initSimpleSender(gateway: CommandGateway,
                       dataEntryProjector: DataEntryProjector,
                       dataEntryCommandSuccessHandler: DataEntryCommandSuccessHandler,
                       dataEntryCommandErrorHandler: DataEntryCommandErrorHandler,
                       objectMapper: ObjectMapper
  ): DataEntryCommandSender {
    return SimpleDataEntryCommandSender(gateway, properties, dataEntryProjector, dataEntryCommandSuccessHandler, dataEntryCommandErrorHandler, objectMapper)
  }
}
