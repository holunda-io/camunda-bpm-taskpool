package io.holunda.camunda.taskpool.example.process

import io.holunda.camunda.datapool.core.EnableDataPool
import io.holunda.camunda.taskpool.core.EnableTaskPool
import io.holunda.camunda.taskpool.example.tasklist.EnableTasklist
import io.holunda.camunda.taskpool.urlresolver.EnablePropertyBasedTaskUrlResolver
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication

fun main(args: Array<String>) {
  SpringApplication.run(ExampleTaskpoolApplication::class.java, *args)
}

@SpringBootApplication
@EnableTaskPool
@EnableDataPool
@EnableTasklist
@EnablePropertyBasedTaskUrlResolver
open class ExampleTaskpoolApplication
