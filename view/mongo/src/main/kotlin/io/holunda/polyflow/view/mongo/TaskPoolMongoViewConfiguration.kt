package io.holunda.polyflow.view.mongo

import io.holunda.polyflow.view.mongo.task.TaskDocument
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

/**
 * Main configuration of the Mongo-DB based view.
 */
@Configuration
@ComponentScan
@EnableReactiveMongoRepositories
@EnableConfigurationProperties(io.holunda.polyflow.view.mongo.TaskPoolMongoViewProperties::class)
@EntityScan(basePackageClasses = [TaskDocument::class])
class TaskPoolMongoViewConfiguration {

  companion object : KLogging() {
    const val COLLECTION_TOKENS = "tracking-tokens"
    const val COLLECTION_EVENTS = "domain-events"
    const val COLLECTION_SAGAS = "sagas"
    const val COLLECTION_SNAPSHOTS = "snapshots"
  }

  /**
   * Report initialization.
   */
  @PostConstruct
  fun info() {
    logger.info { "VIEW-MONGO-001: Initialized mongo view" }
  }

  /**
   * Axon Mongo template configuration.
   */
  @Bean
  fun configureAxonMongoTemplate(databaseFactory: MongoDatabaseFactory): MongoTemplate =
    DefaultMongoTemplate
      .builder()
      .mongoDatabase(databaseFactory.mongoDatabase)
      .trackingTokensCollectionName(COLLECTION_TOKENS)
      // these collections are configured, but not used on the client side.
      .domainEventsCollectionName(COLLECTION_EVENTS)
      .sagasCollectionName(COLLECTION_SAGAS)
      .snapshotEventsCollectionName(COLLECTION_SNAPSHOTS)
      .build()

  /**
   * Axon Mongo Token store configuration.
   */
  @Bean
  fun configureAxonMongoTokenStore(mongoTemplate: MongoTemplate): TokenStore =
    MongoTokenStore
      .builder()
      .mongoTemplate(mongoTemplate)
      .serializer(XStreamSerializer.builder().build())
      .build()

  /**
   * Mongo Type Mapping.
   */
  @Autowired
  fun configureMongoTypeMapping(converter: MappingMongoConverter) {
    // replace "." with ":" (relevant for correlation)
    converter.setMapKeyDotReplacement(":")
  }
}
