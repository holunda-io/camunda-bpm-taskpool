package io.holunda.camunda.taskpool.example.process

import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.holunda.camunda.taskpool.api.task.SourceReference
import org.axonframework.serialization.Serializer
import org.axonframework.serialization.json.JacksonSerializer
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
      .findAndRegisterModules()
      .configureTaskpoolJacksonObjectMapper()
  }

//  @Bean
//  fun eventSerializer(objectMapper: ObjectMapper): Serializer = JacksonSerializer.builder().objectMapper(objectMapper).build()
//
//  @Bean
//  fun messageSerializer(objectMapper: ObjectMapper): Serializer = JacksonSerializer.builder().objectMapper(objectMapper).build()
}

/**
 * Configures Jackson object mapper to handle VariableMap and Source Reference correctly.
 */
fun ObjectMapper.configureTaskpoolJacksonObjectMapper(): ObjectMapper = this
  .registerModule(SimpleModule()
    .apply {
      addAbstractTypeMapping(org.camunda.bpm.engine.variable.VariableMap::class.java, org.camunda.bpm.engine.variable.impl.VariableMapImpl::class.java)
    })
  .apply {
    addMixIn(SourceReference::class.java, KotlinTypeInfo::class.java)
  }


@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "@class", include = JsonTypeInfo.As.PROPERTY)
class KotlinTypeInfo
