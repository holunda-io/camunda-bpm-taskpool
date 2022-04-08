package io.holunda.polyflow.taskpool.core

import com.fasterxml.jackson.databind.ObjectMapper
import io.holunda.polyflow.bus.jackson.configurePolyflowJacksonObjectMapper

/**
 * Configures object mapper.
 */
@Deprecated(
  replaceWith = ReplaceWith("io.holunda.polyflow.bus.jackson.configurePolyflowJacksonObjectMapper()"),
  message = "Moved to separate artifact polyflow-bus-jackson"
)
fun ObjectMapper.configureTaskpoolJacksonObjectMapper(): ObjectMapper = configurePolyflowJacksonObjectMapper()
