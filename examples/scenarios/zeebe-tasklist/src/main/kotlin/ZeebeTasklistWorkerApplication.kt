package io.holuda.taskpool.zeebe.worker.task

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.zeebe.spring.client.EnableZeebeClient
import io.zeebe.spring.client.annotation.ZeebeDeployment
import org.axonframework.commandhandling.CommandResultMessage
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean


fun main(args: Array<String>) {
  SpringApplication.run(ZeebeTasklistWorker::class.java, *args)
}

@SpringBootApplication
@EnableZeebeClient
@ZeebeDeployment(classPathResource = "testworkflow.bpmn")
class ZeebeTasklistWorker {

  /**
   * Default logging handler.
   */
  @Bean
  fun loggingTaskCommandSuccessHandler(): TaskCommandSuccessHandler = LoggingTaskCommandSuccessHandler(LoggerFactory.getLogger(ZeebeTasklistWorker::class.java))

  /**
   * Default logging handler.
   */
  @Bean
  fun loggingTaskCommandErrorHandler(): TaskCommandErrorHandler = LoggingTaskCommandErrorHandler(LoggerFactory.getLogger(ZeebeTasklistWorker::class.java))


  @Bean
  fun objectMapper() : ObjectMapper = jacksonObjectMapper()
}

