package io.holunda.polyflow.bus.jackson

import com.fasterxml.jackson.databind.ObjectMapper
import io.holunda.camunda.taskpool.api.task.SourceReference

/**
 * Configures object mapper.
 */
fun ObjectMapper.configurePolyflowJacksonObjectMapper(): ObjectMapper = this
  .registerModule(VariableMapTypeMappingModule())
  .registerModule(DataEntryStateTypeMappingModule())
  .apply {
    addMixIn(SourceReference::class.java, KotlinTypeInfo::class.java)
  }

/**
 * Helper to configure an existing object mapper from Java.
 */
class ObjectMapperConfigurationHelper {
  companion object {
    /**
     * Registers modules required fo polyflow.
     * @param objectMapper object mapper to configure.
     * @return configured object mapper.
     */
    @JvmStatic
    fun configurePolyflowJacksonObjectMapper(objectMapper: ObjectMapper): ObjectMapper {
      return objectMapper.configurePolyflowJacksonObjectMapper()
    }
  }
}
