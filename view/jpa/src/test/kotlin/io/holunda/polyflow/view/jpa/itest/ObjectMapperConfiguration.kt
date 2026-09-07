package io.holunda.polyflow.view.jpa.itest

import tools.jackson.module.kotlin.jacksonObjectMapper
import tools.jackson.databind.json.JsonMapper
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import tools.jackson.databind.cfg.DateTimeFeature

import java.text.SimpleDateFormat

@Configuration
class ObjectMapperConfiguration {
  @Bean
  fun objectMapper() = jacksonObjectMapper()
    .rebuild<JsonMapper, JsonMapper.Builder>()
    .defaultDateFormat(SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'"))
    .disable(DateTimeFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS)
    .build()

}
