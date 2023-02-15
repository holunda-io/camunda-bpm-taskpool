package io.holunda.polyflow.bus.jackson

import com.fasterxml.jackson.databind.ObjectMapper
import io.holunda.camunda.taskpool.api.business.AuthorizationChange
import io.holunda.camunda.taskpool.api.task.SourceReference
import io.holunda.polyflow.view.filter.Criterion
import io.holunda.polyflow.view.query.process.variable.ProcessVariableFilter

/**
 * Configures object mapper.
 */
fun ObjectMapper.configurePolyflowJacksonObjectMapper(): ObjectMapper = this
  /*
   * List all custom modules.
   */
  .registerModule(VariableMapTypeMappingModule())
  .registerModule(DataEntryStateTypeMappingModule())
  .apply {
    /*
     * List here all interfaces used in messages, which have multiple implementations and require additional
     * type descriminator.
     */
    addMixIn(SourceReference::class.java, KotlinTypeInfo::class.java)
    addMixIn(AuthorizationChange::class.java, KotlinTypeInfo::class.java)
    addMixIn(Criterion::class.java, KotlinTypeInfo::class.java)
    addMixIn(ProcessVariableFilter::class.java, KotlinTypeInfo::class.java)
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
