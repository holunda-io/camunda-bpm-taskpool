package io.holunda.polyflow.taskpool.core

import com.fasterxml.jackson.databind.ObjectMapper
import io.holunda.polyflow.bus.jackson.configureTaskpoolJacksonObjectMapper as configureJacksonBusObjectMapper

/**
 * Configures object mapper.
 */
@Deprecated(
  replaceWith = ReplaceWith("io.holunda.polyflow.bus.jackson.configureTaskpoolJacksonObjectMapper()"),
  message = "Moved to separate artifact polyflow-bus-jackson"
)
fun ObjectMapper.configureTaskpoolJacksonObjectMapper(): ObjectMapper = configureJacksonBusObjectMapper()
