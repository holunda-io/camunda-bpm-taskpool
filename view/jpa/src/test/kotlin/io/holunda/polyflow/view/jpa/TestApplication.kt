package io.holunda.polyflow.view.jpa

import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean
import java.text.SimpleDateFormat

@SpringBootApplication
class TestApplication {

  @Bean
  fun objectMapper() = jacksonObjectMapper().apply {
    dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'")
    registerModule(JavaTimeModule())
    configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
    configure(SerializationFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS, false)
  }
}
