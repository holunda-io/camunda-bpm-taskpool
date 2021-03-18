package io.holunda.camunda.taskpool.view.mongo

import com.mongodb.client.MongoClient
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
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.data.convert.ConfigurableTypeInformationMapper
import org.springframework.data.mongodb.MongoDatabaseFactory
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration
import org.springframework.data.mongodb.core.convert.DefaultMongoTypeMapper
import org.springframework.data.mongodb.core.convert.DefaultMongoTypeMapper.DEFAULT_TYPE_KEY
import org.springframework.data.mongodb.core.convert.MappingMongoConverter
import org.springframework.data.mongodb.core.convert.MongoCustomConversions
import org.springframework.data.mongodb.core.mapping.MongoMappingContext
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories
import javax.annotation.PostConstruct


@Configuration
@ComponentScan
@EnableReactiveMongoRepositories
@EnableConfigurationProperties(TaskPoolMongoViewProperties::class)
class TaskPoolMongoViewConfiguration : AbstractMongoClientConfiguration() {

  companion object : KLogging()

  @Value("\${spring.data.mongodb.database:tasks-control}")
  lateinit var mongoDatabaseName: String

  @PostConstruct
  fun info() {
    logger.info { "VIEW-MONGO-001: Initialized mongo view to use database '$mongoDatabaseName'" }
  }

  @Bean
  fun configureAxonMongoTemplate(): MongoTemplate =
    DefaultMongoTemplate
      .builder()
      .mongoDatabase(mongoClient(), mongoDatabaseName)
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

  @Bean
  override fun mappingMongoConverter(mongoDatabaseFactory: MongoDatabaseFactory, customConversions: MongoCustomConversions, mappingContext: MongoMappingContext): MappingMongoConverter {
    val mmc = super.mappingMongoConverter(mongoDatabaseFactory, customConversions, mappingContext)
    mmc.apply {
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
    return mmc
  }

  override fun getDatabaseName(): String = this.mongoDatabaseName
}
