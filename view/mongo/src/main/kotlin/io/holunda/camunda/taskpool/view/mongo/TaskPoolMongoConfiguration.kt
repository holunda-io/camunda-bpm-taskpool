package io.holunda.camunda.taskpool.view.mongo

import com.mongodb.reactivestreams.client.MongoClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.mongodb.core.ReactiveMongoTemplate


@Configuration
open class TaskPoolMongoConfiguration {

  @Bean
  open fun configureReactiveTemplate(mongoClient: MongoClient): ReactiveMongoTemplate = ReactiveMongoTemplate(mongoClient, "tasks")
}
