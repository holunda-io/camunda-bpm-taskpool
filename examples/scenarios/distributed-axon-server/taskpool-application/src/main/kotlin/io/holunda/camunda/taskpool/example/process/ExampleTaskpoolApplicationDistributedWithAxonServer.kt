package io.holunda.camunda.taskpool.example.process

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.holixon.axon.gateway.query.RevisionValue
import io.holunda.camunda.datapool.core.EnableDataPool
import io.holunda.camunda.taskpool.core.EnableTaskPool
import io.holunda.camunda.taskpool.core.configureTaskpoolJacksonObjectMapper
import io.holunda.camunda.taskpool.example.tasklist.EnableTasklist
import io.holunda.camunda.taskpool.example.users.UsersConfiguration
import io.holunda.polyflow.urlresolver.EnablePropertyBasedFormUrlResolver
import org.axonframework.commandhandling.CommandMessage
import org.axonframework.messaging.correlation.CorrelationDataProvider
import org.axonframework.messaging.correlation.MessageOriginProvider
import org.axonframework.messaging.correlation.MultiCorrelationDataProvider
import org.axonframework.messaging.correlation.SimpleCorrelationDataProvider
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
      .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS) // let the dates be strings and not nanoseconds
      .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES) // be nice to properties we don't understand
  }

//  @Bean
//  fun eventSerializer(objectMapper: ObjectMapper): Serializer = JacksonSerializer.builder().objectMapper(objectMapper).build()
//
//  @Bean
//  fun messageSerializer(objectMapper: ObjectMapper): Serializer = JacksonSerializer.builder().objectMapper(objectMapper).build()


  /**
   * Factory function creating correlation data provider for revision information.
   * We don't want to explicitly pump revision meta data from command to event.
   */
  @Bean
  fun revisionAwareCorrelationDataProvider(): CorrelationDataProvider {
    return MultiCorrelationDataProvider<CommandMessage<Any>>(
      listOf(
        MessageOriginProvider(),
        SimpleCorrelationDataProvider(RevisionValue.REVISION_KEY)
      )
    )
  }
}


