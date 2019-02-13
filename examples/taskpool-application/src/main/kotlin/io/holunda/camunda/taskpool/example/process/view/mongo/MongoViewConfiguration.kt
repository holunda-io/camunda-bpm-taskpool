package io.holunda.camunda.taskpool.example.process.view.mongo

import io.holunda.camunda.taskpool.view.mongo.EnableTaskPoolMongoView
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile


@Configuration
@Profile("mongo")
@EnableTaskPoolMongoView
open class MongoViewConfiguration
