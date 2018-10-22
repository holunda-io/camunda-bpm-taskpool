package io.holunda.camunda.datapool

import com.fasterxml.jackson.databind.ObjectMapper
import io.holunda.camunda.datapool.sender.simple.SimpleDataEntryCommandSender
import io.holunda.camunda.taskpool.api.sender.DataEntryCommandSender
import org.axonframework.commandhandling.gateway.CommandGateway
import org.springframework.beans.factory.annotation.Autowired
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
open class DataEntrySenderConfiguration {

  @Autowired
  lateinit var gateway: CommandGateway

  @Autowired
  lateinit var properties: DataEntrySenderProperties

  @Autowired
  lateinit var objectMapper: ObjectMapper

  @Bean
  @ConditionalOnProperty(name = ["camunda.taskpool.dataentry.sender.type"], havingValue = "simple")
  open fun initSimpleSender(): DataEntryCommandSender {
    return SimpleDataEntryCommandSender(gateway, properties, objectMapper)
  }

}
