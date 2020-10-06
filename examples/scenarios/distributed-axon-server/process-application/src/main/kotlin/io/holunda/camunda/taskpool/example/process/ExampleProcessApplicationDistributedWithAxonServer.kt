package io.holunda.camunda.taskpool.example.process

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.holunda.camunda.taskpool.configureTaskpoolJacksonObjectMapper
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean


fun main(args: Array<String>) {
  SpringApplication.run(ExampleProcessApplicationDistributedWithAxonServer::class.java, *args)
}

@SpringBootApplication
class ExampleProcessApplicationDistributedWithAxonServer {

  @Bean
  fun objectMapper(): ObjectMapper {
    return jacksonObjectMapper()
      .findAndRegisterModules()!!
      .configureTaskpoolJacksonObjectMapper()
  }

//  @Bean
//  fun eventSerializer(objectMapper: ObjectMapper): Serializer = JacksonSerializer.builder().objectMapper(objectMapper).build()
//
//  @Bean
//  fun messageSerializer(objectMapper: ObjectMapper): Serializer = JacksonSerializer.builder().objectMapper(objectMapper).build()
}
