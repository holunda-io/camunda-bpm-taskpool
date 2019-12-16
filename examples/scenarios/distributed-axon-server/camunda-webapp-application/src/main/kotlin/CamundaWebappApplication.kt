package io.holunda.taskpool.camunda.webapp

import org.camunda.bpm.spring.boot.starter.annotation.EnableProcessApplication
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication


fun main(args: Array<String>) {
  SpringApplication.run(CamundaWebappApplication::class.java, *args)
}

@SpringBootApplication
@EnableProcessApplication
class CamundaWebappApplication
