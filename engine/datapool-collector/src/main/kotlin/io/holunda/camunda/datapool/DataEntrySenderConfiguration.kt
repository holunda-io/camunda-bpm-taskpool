package io.holunda.camunda.datapool

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
 * Configuration enabled by the property
 */
@ComponentScan
@Configuration
@EnableConfigurationProperties(DataEntrySenderProperties::class)
class DataEntrySenderConfiguration {

  @Autowired
  lateinit var properties: DataEntrySenderProperties

  /**
   * Default configuration of the simple sender.
   */
  @Bean
  @ConditionalOnProperty(name = ["camunda.taskpool.dataentry.sender.type"], havingValue = "simple")
  fun initSimpleSender(gateway: CommandGateway,
                       dataEntryProjector: DataEntryProjector,
                       dataEntryCommandSuccessHandler: DataEntryCommandSuccessHandler,
                       dataEntryCommandErrorHandler: DataEntryCommandErrorHandler
  ): DataEntryCommandSender {
    return SimpleDataEntryCommandSender(gateway, properties, dataEntryProjector, dataEntryCommandSuccessHandler, dataEntryCommandErrorHandler)
  }
}
