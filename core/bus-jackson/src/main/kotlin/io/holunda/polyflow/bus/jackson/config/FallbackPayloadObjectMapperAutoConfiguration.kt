package io.holunda.polyflow.bus.jackson.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.github.oshai.kotlinlogging.KotlinLogging
import io.holunda.polyflow.bus.jackson.annotation.ConditionalOnMissingQualifiedBean
import io.holunda.polyflow.bus.jackson.configurePolyflowJacksonObjectMapper
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean

private val logger = KotlinLogging.logger {}

/**
 * No @configuration required, used as autoconfiguration.
 */
class FallbackPayloadObjectMapperAutoConfiguration {

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
    .findAndRegisterModules().apply {
      logger.warn { "Fallback polyflow objectMapper is used, consider to provide an object mapper bean with qualifier '$PAYLOAD_OBJECT_MAPPER'" }
    }
}
