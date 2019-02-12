package io.holunda.camunda.taskpool.view.mongo

import com.mongodb.MongoClient
import io.holunda.camunda.taskpool.view.mongo.service.TaskPoolMongoService
import org.axonframework.eventhandling.tokenstore.TokenStore
import org.axonframework.extensions.mongo.DefaultMongoTemplate
import org.axonframework.extensions.mongo.MongoTemplate
import org.axonframework.extensions.mongo.eventsourcing.tokenstore.MongoTokenStore
import org.axonframework.serialization.xml.XStreamSerializer
import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.data.mongodb.MongoDbFactory
import org.springframework.data.mongodb.core.convert.DefaultDbRefResolver
import org.springframework.data.mongodb.core.convert.MappingMongoConverter
import org.springframework.data.mongodb.core.mapping.MongoMappingContext
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories

@Configuration
@ComponentScan
@EnableMongoRepositories
open class TaskPoolMongoViewConfiguration {

  @Bean
  open fun configure(mongoTemplate: MongoTemplate): TokenStore =
    MongoTokenStore
      .builder()
      .mongoTemplate(mongoTemplate)
      .serializer(XStreamSerializer.builder().build())
      .build()

  @Bean
  open fun configureAxonMongoTemplate(mongoClient: MongoClient): MongoTemplate =
    DefaultMongoTemplate
      .builder()
      .mongoDatabase(mongoClient, "axon")
      .trackingTokensCollectionName("trackingTokens")
      .domainEventsCollectionName("domainEvents")
      .sagasCollectionName("sagas")
      .snapshotEventsCollectionName("snapshots")
      .build()

  @Bean
  open fun mongoConverter(mongoFactory: MongoDbFactory) = MappingMongoConverter(DefaultDbRefResolver(mongoFactory), MongoMappingContext())
    .apply {
      setMapKeyDotReplacement(":")
    }


  // @Bean
  // @ConditionalOnProperty(prefix = "camunda.taskpool.view.mongo", name = ["replay"], value = ["true"], matchIfMissing = true)
  open fun initializeSimpleView(taskPoolMongoService: TaskPoolMongoService) = ApplicationRunner {
    taskPoolMongoService.restore()
  }

}
