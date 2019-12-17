package io.holunda.camunda.taskpool.example.process

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication


fun main(args: Array<String>) {
  SpringApplication.run(ExampleProcessApplicationDistributedWithAxonServer::class.java, *args)
}

@SpringBootApplication
class ExampleProcessApplicationDistributedWithAxonServer
