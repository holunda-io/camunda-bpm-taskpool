package io.holunda.camunda.taskpool.example.process

import io.holunda.camunda.taskpool.EnableTaskCollector
import io.holunda.camunda.taskpool.core.EnableTaskPool
import io.holunda.camunda.taskpool.plugin.EnableCamundaSpringEventing
import io.holunda.camunda.taskpool.view.simple.EnableTaskPoolSimpleView
import org.camunda.bpm.spring.boot.starter.annotation.EnableProcessApplication
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication


fun main(args: Array<String>) {
  SpringApplication.run(ExampleProcessApplication::class.java, *args)
}

@SpringBootApplication
@EnableProcessApplication
@EnableCamundaSpringEventing
@EnableTaskCollector
@EnableTaskPool
@EnableTaskPoolSimpleView
open class ExampleProcessApplication
