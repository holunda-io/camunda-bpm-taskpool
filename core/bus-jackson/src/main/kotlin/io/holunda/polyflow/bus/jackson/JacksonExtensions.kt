package io.holunda.polyflow.bus.jackson

import com.fasterxml.jackson.databind.ObjectMapper
import io.holunda.camunda.taskpool.api.task.SourceReference

/**
 * Configures object mapper.
 */
fun ObjectMapper.configureTaskpoolJacksonObjectMapper(): ObjectMapper = this
  .registerModule(VariableMapTypeMappingModule())
  .registerModule(DataEntryStateTypeMappingModule())
  .apply {
    addMixIn(SourceReference::class.java, KotlinTypeInfo::class.java)
  }

