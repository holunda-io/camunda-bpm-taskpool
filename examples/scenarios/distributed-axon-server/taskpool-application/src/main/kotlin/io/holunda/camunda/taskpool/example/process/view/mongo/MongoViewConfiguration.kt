package io.holunda.camunda.taskpool.example.process.view.mongo

import io.holunda.camunda.taskpool.view.mongo.EnableTaskPoolMongoView
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Profile

@Configuration
@Profile("mongo")
@EnableTaskPoolMongoView
@Import(
  value = [
    org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration::class,
    org.springframework.boot.autoconfigure.mongo.MongoReactiveAutoConfiguration::class,
    org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration::class,
    org.springframework.boot.autoconfigure.data.mongo.MongoReactiveDataAutoConfiguration::class
  ])
class MongoViewConfiguration
