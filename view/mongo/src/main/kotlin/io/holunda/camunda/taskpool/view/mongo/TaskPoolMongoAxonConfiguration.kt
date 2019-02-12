package io.holunda.camunda.taskpool.view.mongo

import com.mongodb.MongoClient
import org.axonframework.eventhandling.tokenstore.TokenStore
import org.axonframework.extensions.mongo.DefaultMongoTemplate
import org.axonframework.extensions.mongo.MongoTemplate
import org.axonframework.extensions.mongo.eventsourcing.tokenstore.MongoTokenStore
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class TaskPoolMongoAxonConfiguration {


  @Bean
  open fun configure(mongoTemplate: MongoTemplate): TokenStore = MongoTokenStore.builder().mongoTemplate(mongoTemplate).build()

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

}
