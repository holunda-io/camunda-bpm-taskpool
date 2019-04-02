package io.holunda.camunda.taskpool.view.mongo

import com.mongodb.MongoClient
import io.holunda.camunda.taskpool.view.mongo.repository.CaseReferenceDocument
import io.holunda.camunda.taskpool.view.mongo.repository.ProcessReferenceDocument
import io.holunda.camunda.taskpool.view.mongo.repository.ReferenceDocument
import mu.KLogging
import org.axonframework.eventhandling.tokenstore.TokenStore
import org.axonframework.extensions.mongo.DefaultMongoTemplate
import org.axonframework.extensions.mongo.MongoTemplate
import org.axonframework.extensions.mongo.eventsourcing.tokenstore.MongoTokenStore
import org.axonframework.serialization.xml.XStreamSerializer
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.data.convert.ConfigurableTypeInformationMapper
import org.springframework.data.mongodb.MongoDbFactory
import org.springframework.data.mongodb.core.convert.DefaultDbRefResolver
import org.springframework.data.mongodb.core.convert.DefaultMongoTypeMapper
import org.springframework.data.mongodb.core.convert.DefaultMongoTypeMapper.DEFAULT_TYPE_KEY
import org.springframework.data.mongodb.core.convert.MappingMongoConverter
import org.springframework.data.mongodb.core.mapping.MongoMappingContext
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories
import javax.annotation.PostConstruct

@Configuration
@ComponentScan
@EnableMongoRepositories
open class TaskPoolMongoViewConfiguration {

  companion object : KLogging()

  @Value("\${spring.data.mongodb.database:tasks-control}")
  lateinit var mongoDatabaseName: String

  @PostConstruct
  open fun info() {
    logger.info { "VIEW-MONGO-001: Initialized mongo view" }
  }

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
      .mongoDatabase(mongoClient, mongoDatabaseName)
      .trackingTokensCollectionName("tracking-tokens")
      // these collections are configured, but not used on the client side.
      .domainEventsCollectionName("domain-events")
      .sagasCollectionName("sagas")
      .snapshotEventsCollectionName("snapshots")
      .build()

  @Bean
  open fun mongoConverter(mongoFactory: MongoDbFactory) = MappingMongoConverter(
    DefaultDbRefResolver(mongoFactory),
    MongoMappingContext()
  )
    .apply {
      this.typeMapper = DefaultMongoTypeMapper(DEFAULT_TYPE_KEY, listOf(
        // register type aliases for source references.
        ConfigurableTypeInformationMapper(
          mapOf(
            ProcessReferenceDocument::class.java to ReferenceDocument.PROCESS,
            CaseReferenceDocument::class.java to ReferenceDocument.CASE
          )
        )
      ))
      // replace "." with ":" (relevant for correlation)
      this.setMapKeyDotReplacement(":")
    }

}
