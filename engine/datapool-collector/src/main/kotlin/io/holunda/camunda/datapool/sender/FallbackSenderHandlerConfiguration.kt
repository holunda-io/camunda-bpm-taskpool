package io.holunda.camunda.datapool.sender

import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
// @ConditionalOnMissingBean(value = [DataEntryCommandSuccessHandler::class, DataEntryCommandErrorHandler::class])
class FallbackSenderHandlerConfiguration {

  @Bean
  @ConditionalOnMissingBean
  fun loggingDataEntryCommandSuccessHandler() = LoggingCommandSuccessHandler(LoggerFactory.getLogger(DataEntryCommandSender::class.java))

  @Bean
  @ConditionalOnMissingBean
  fun loggingDataEntryCommandErrorHandler() = LoggingCommandErrorHandler(LoggerFactory.getLogger(DataEntryCommandSender::class.java))

}
