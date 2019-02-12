package io.holunda.camunda.taskpool.example.process.mongo

import io.holunda.camunda.taskpool.view.mongo.EnableTaskPoolMongoView
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.data.mongodb.MongoDbFactory
import org.springframework.data.mongodb.core.convert.DefaultDbRefResolver
import org.springframework.data.mongodb.core.convert.MappingMongoConverter
import org.springframework.data.mongodb.core.mapping.MongoMappingContext


@Configuration
@Profile("mongo")
@EnableTaskPoolMongoView
open class MongoConfiguration
