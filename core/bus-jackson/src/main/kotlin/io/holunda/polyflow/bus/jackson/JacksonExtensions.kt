package io.holunda.polyflow.bus.jackson

import tools.jackson.databind.ObjectMapper
import tools.jackson.databind.json.JsonMapper
import io.holunda.camunda.taskpool.api.business.AuthorizationChange
import io.holunda.camunda.taskpool.api.task.SourceReference
import io.holunda.polyflow.view.filter.Criterion
import io.holunda.polyflow.view.query.process.variable.ProcessVariableFilter

/**
 * Configures object mapper.
 */
fun ObjectMapper.configurePolyflowJacksonObjectMapper(): ObjectMapper = this
  .rebuild<JsonMapper, JsonMapper.Builder>()
  .addModule(VariableMapTypeMappingModule())
  .addModule(DataEntryStateTypeMappingModule())
  .addMixIn(SourceReference::class.java, KotlinTypeInfo::class.java)
  .addMixIn(AuthorizationChange::class.java, KotlinTypeInfo::class.java)
  .addMixIn(Criterion::class.java, KotlinTypeInfo::class.java)
  .addMixIn(ProcessVariableFilter::class.java, KotlinTypeInfo::class.java)
  .build()

/**
 * Helper to configure an existing object mapper from Java.
 */
class ObjectMapperConfigurationHelper {
  /**
   * Static methods.
   */
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
