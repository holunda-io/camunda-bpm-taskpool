package io.holunda.camunda.taskpool.example.process

import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.holunda.camunda.datapool.core.EnableDataPool
import io.holunda.camunda.taskpool.api.task.SourceReference
import io.holunda.camunda.taskpool.core.EnableTaskPool
import io.holunda.camunda.taskpool.example.tasklist.EnableTasklist
import io.holunda.camunda.taskpool.example.users.UsersConfiguration
import io.holunda.camunda.taskpool.upcast.definition.ProcessDefinitionEventNullTo1Upcaster
import io.holunda.camunda.taskpool.urlresolver.EnablePropertyBasedFormUrlResolver
import org.axonframework.serialization.upcasting.event.EventUpcaster
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration
import org.springframework.boot.autoconfigure.data.mongo.MongoReactiveDataAutoConfiguration
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration
import org.springframework.boot.autoconfigure.mongo.MongoReactiveAutoConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import

fun main(args: Array<String>) {
  SpringApplication.run(ExampleTaskpoolApplicationDistributedWithAxonServer::class.java, *args)
}

@SpringBootApplication(exclude = [
  MongoAutoConfiguration::class,
  MongoReactiveAutoConfiguration::class,
  MongoDataAutoConfiguration::class,
  MongoReactiveDataAutoConfiguration::class
])
@Import(
  UsersConfiguration::class
)
@EnableTaskPool
@EnableDataPool
@EnableTasklist
@EnablePropertyBasedFormUrlResolver
class ExampleTaskpoolApplicationDistributedWithAxonServer {

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

  @Bean
  fun processDefinitionEventUpcaster(): EventUpcaster = ProcessDefinitionEventNullTo1Upcaster()


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

}


