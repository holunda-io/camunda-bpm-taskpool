package io.holunda.camunda.taskpool.example.process.view.mongo

import io.holunda.camunda.taskpool.view.mongo.EnableTaskPoolMongoView
import org.springframework.boot.autoconfigure.ImportAutoConfiguration
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration
import org.springframework.boot.autoconfigure.data.mongo.MongoReactiveDataAutoConfiguration
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration
import org.springframework.boot.autoconfigure.mongo.MongoReactiveAutoConfiguration
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
@Profile("mongo")
@EnableTaskPoolMongoView
@ImportAutoConfiguration(value = [
  MongoAutoConfiguration::class,
  MongoDataAutoConfiguration::class,
  MongoReactiveAutoConfiguration::class,
  MongoReactiveDataAutoConfiguration::class
])
open class MongoViewConfiguration
