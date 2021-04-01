package io.holunda.camunda.taskpool.view.mongo

import io.holunda.camunda.taskpool.view.mongo.repository.TaskDocument
import mu.KLogging
import org.axonframework.eventhandling.tokenstore.TokenStore
import org.axonframework.extensions.mongo.DefaultMongoTemplate
import org.axonframework.extensions.mongo.MongoTemplate
import org.axonframework.extensions.mongo.eventsourcing.tokenstore.MongoTokenStore
import org.axonframework.serialization.xml.XStreamSerializer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.data.mongodb.MongoDatabaseFactory
import org.springframework.data.mongodb.core.convert.MappingMongoConverter
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories
import javax.annotation.PostConstruct


@Configuration
@ComponentScan
@EnableReactiveMongoRepositories
@EnableConfigurationProperties(TaskPoolMongoViewProperties::class)
@EntityScan(basePackageClasses = [TaskDocument::class])
class TaskPoolMongoViewConfiguration {

  companion object : KLogging()

  @PostConstruct
  fun info() {
    logger.info { "VIEW-MONGO-001: Initialized mongo view" }
  }

  @Bean
  fun configureAxonMongoTemplate(databaseFactory: MongoDatabaseFactory): MongoTemplate =
    DefaultMongoTemplate
      .builder()
      .mongoDatabase(databaseFactory.mongoDatabase)
      .trackingTokensCollectionName("tracking-tokens")
      // these collections are configured, but not used on the client side.
      .domainEventsCollectionName("domain-events")
      .sagasCollectionName("sagas")
      .snapshotEventsCollectionName("snapshots")
      .build()

  @Bean
  fun configureAxonMongoTokenStore(mongoTemplate: MongoTemplate): TokenStore =
    MongoTokenStore
      .builder()
      .mongoTemplate(mongoTemplate)
      .serializer(XStreamSerializer.builder().build())
      .build()

  @Autowired
  fun configureMongoTypeMapping(converter: MappingMongoConverter) {
    // replace "." with ":" (relevant for correlation)
    converter.setMapKeyDotReplacement(":")
  }
}
