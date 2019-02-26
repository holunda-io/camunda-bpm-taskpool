package io.holunda.camunda.taskpool.view.mongo

import org.springframework.boot.autoconfigure.ImportAutoConfiguration
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration
import org.springframework.boot.autoconfigure.data.mongo.MongoReactiveDataAutoConfiguration
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration
import org.springframework.boot.autoconfigure.mongo.MongoReactiveAutoConfiguration
import org.springframework.context.annotation.Import

/**
 * Enables mongo db taskpool view
 */
@MustBeDocumented
@Import(TaskPoolMongoViewConfiguration::class)
annotation class EnableTaskPoolMongoView
