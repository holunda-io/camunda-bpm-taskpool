package io.holunda.camunda.taskpool.example.process

import io.holunda.camunda.taskpool.EnableTaskCollector
import io.holunda.camunda.taskpool.core.EnableTaskPool
import io.holunda.camunda.taskpool.enricher.FilterType
import io.holunda.camunda.taskpool.enricher.ProcessVariableFilter
import io.holunda.camunda.taskpool.enricher.ProcessVariablesFilter
import io.holunda.camunda.taskpool.example.process.process.ProcessApproveRequest
import io.holunda.camunda.taskpool.plugin.EnableCamundaSpringEventing
import io.holunda.camunda.taskpool.view.simple.EnableTaskPoolSimpleView
import org.camunda.bpm.spring.boot.starter.annotation.EnableProcessApplication
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean


fun main(args: Array<String>) {
  SpringApplication.run(ExampleProcessApplication::class.java, *args)
}

@SpringBootApplication
@EnableProcessApplication
@EnableCamundaSpringEventing
@EnableTaskCollector
@EnableTaskPool
@EnableTaskPoolSimpleView
open class ExampleProcessApplication {

  @Bean
  open fun processVariablesFilter() = ProcessVariablesFilter(
    ProcessVariableFilter(ProcessApproveRequest.KEY, FilterType.INCLUDE, mapOf(
      ProcessApproveRequest.Elements.AMEND_REQUEST
        to listOf(
        ProcessApproveRequest.Variables.ON_BEHALF,
        ProcessApproveRequest.Variables.ORIGINATOR,
        ProcessApproveRequest.Variables.SUBJECT),
      ProcessApproveRequest.Elements.APPROVE_REQUEST
        to listOf(
        ProcessApproveRequest.Variables.TARGET)
    ))
  )
}


