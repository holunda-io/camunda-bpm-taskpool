package io.holunda.polyflow.example.process.platform

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.holixon.axon.gateway.query.RevisionValue
import io.holunda.polyflow.datapool.core.EnablePolyflowDataPool
import io.holunda.polyflow.example.tasklist.EnableTasklist
import io.holunda.polyflow.example.users.UsersConfiguration
import io.holunda.polyflow.taskpool.core.EnablePolyflowTaskPool
import io.holunda.polyflow.taskpool.core.configureTaskpoolJacksonObjectMapper
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

/**
 * Starts platform application.
 */
fun main(args: Array<String>) {
  SpringApplication.run(ExamplePlatformApplicationDistributedWithAxonServer::class.java, *args)
}

/**
 * Process application using Axon Server as event store and communication platform.
 * Includes:
 * - core/taskpool
 * - core/datapool
 * - view
 * - tasklist backend
 */
@SpringBootApplication(
  exclude = [
    MongoAutoConfiguration::class,
    MongoReactiveAutoConfiguration::class,
    MongoDataAutoConfiguration::class,
    MongoReactiveDataAutoConfiguration::class
  ]
)
@Import(
  UsersConfiguration::class
)
@EnablePolyflowTaskPool
@EnablePolyflowDataPool
@EnableTasklist
@EnablePropertyBasedFormUrlResolver
class ExamplePlatformApplicationDistributedWithAxonServer {

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


