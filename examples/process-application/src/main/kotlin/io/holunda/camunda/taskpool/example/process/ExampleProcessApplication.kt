package io.holunda.camunda.taskpool.example.process

import io.holunda.camunda.taskpool.TaskCollectorConfiguration
import io.holunda.camunda.taskpool.core.TaskPoolCoreConfiguration
import io.holunda.camunda.taskpool.plugin.CamundaEventingConfiguration
import org.camunda.bpm.spring.boot.starter.annotation.EnableProcessApplication
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Import


fun main(args: Array<String>) {
  SpringApplication.run(ExampleProcessApplication::class.java, *args)
}

@SpringBootApplication
@EnableProcessApplication
@Import(CamundaEventingConfiguration::class, TaskCollectorConfiguration::class, TaskPoolCoreConfiguration::class)
open class ExampleProcessApplication()
