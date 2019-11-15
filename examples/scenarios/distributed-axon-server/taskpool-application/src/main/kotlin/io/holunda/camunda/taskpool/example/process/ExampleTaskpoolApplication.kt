package io.holunda.camunda.taskpool.example.process

import io.holunda.camunda.datapool.core.EnableDataPool
import io.holunda.camunda.taskpool.core.EnableTaskPool
import io.holunda.camunda.taskpool.example.tasklist.EnableTasklist
import io.holunda.camunda.taskpool.example.users.UsersConfiguration
import io.holunda.camunda.taskpool.urlresolver.EnablePropertyBasedFormUrlResolver
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration
import org.springframework.boot.autoconfigure.data.mongo.MongoReactiveDataAutoConfiguration
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration
import org.springframework.boot.autoconfigure.mongo.MongoReactiveAutoConfiguration
import org.springframework.context.annotation.Import

fun main(args: Array<String>) {
  SpringApplication.run(ExampleTaskpoolApplication::class.java, *args)
}

@SpringBootApplication(exclude = [
  MongoAutoConfiguration::class,
  MongoReactiveAutoConfiguration::class,
  MongoDataAutoConfiguration::class,
  MongoReactiveDataAutoConfiguration::class
])
@Import(
  UsersConfiguration::class
)
@EnableTaskPool
@EnableDataPool
@EnableTasklist
@EnablePropertyBasedFormUrlResolver
class ExampleTaskpoolApplication
