package io.holunda.polyflow.bus.jackson.config

import tools.jackson.databind.ObjectMapper
import tools.jackson.databind.json.JsonMapper
import tools.jackson.module.kotlin.jacksonObjectMapper
import io.github.oshai.kotlinlogging.KotlinLogging
import io.holunda.polyflow.bus.jackson.configurePolyflowJacksonObjectMapper
import io.toolisticon.spring.condition.ConditionalOnMissingQualifiedBean
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

private val logger = KotlinLogging.logger {}

/**
 * No @configuration required, used as autoconfiguration.
 */
@Configuration
class FallbackPayloadObjectMapperAutoConfiguration {

  /**
   * Static constants for bean names.
   */
  companion object {
    const val PAYLOAD_OBJECT_MAPPER = "payloadObjectMapper"
  }

  /**
   * Conditional object mapper, if not defined by the user.
   */
  @Bean
  @Qualifier(PAYLOAD_OBJECT_MAPPER)
  @ConditionalOnMissingQualifiedBean(beanClass = ObjectMapper::class, qualifier = PAYLOAD_OBJECT_MAPPER)
  fun taskCollectorObjectMapper(): ObjectMapper = jacksonObjectMapper()
    .configurePolyflowJacksonObjectMapper()
    .rebuild<JsonMapper, JsonMapper.Builder>()
    .findAndAddModules()
    .build().apply {
      logger.warn { "Fallback polyflow objectMapper is used, consider to provide an object mapper bean with qualifier '$PAYLOAD_OBJECT_MAPPER'" }
    }
}
