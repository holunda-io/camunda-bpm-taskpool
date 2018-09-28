package io.holunda.camunda.taskpool.example.process

import io.holunda.camunda.taskpool.EnableTaskCollector
import io.holunda.camunda.taskpool.core.EnableTaskPool
import io.holunda.camunda.taskpool.enricher.*
import io.holunda.camunda.taskpool.example.process.process.ProcessApproveRequest
import io.holunda.camunda.taskpool.plugin.EnableCamundaSpringEventing
import io.holunda.camunda.taskpool.view.simple.EnableTaskPoolSimpleView
import mu.KLogging
import org.camunda.bpm.spring.boot.starter.annotation.EnableProcessApplication
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean
import org.springframework.web.reactive.config.EnableWebFlux


fun main(args: Array<String>) {
  SpringApplication.run(ExampleProcessApplication::class.java, *args)
}

@SpringBootApplication
@EnableProcessApplication
@EnableCamundaSpringEventing
@EnableTaskCollector
@EnableTaskPool
@EnableTaskPoolSimpleView
@EnableWebFlux
open class ExampleProcessApplication {

  companion object : KLogging()

  @Bean
  open fun processVariablesFilter() = ProcessVariablesFilter(

    // define a filter for every process
    ProcessVariableFilter(ProcessApproveRequest.KEY, FilterType.INCLUDE, mapOf(

      // define a filter for every task
      ProcessApproveRequest.Elements.APPROVE_REQUEST to listOf(
        ProcessApproveRequest.Variables.REQUEST_ID,
        ProcessApproveRequest.Variables.TARGET
      ),

      // and again
      ProcessApproveRequest.Elements.AMEND_REQUEST to listOf(
        ProcessApproveRequest.Variables.REQUEST_ID,
        ProcessApproveRequest.Variables.ON_BEHALF,
        ProcessApproveRequest.Variables.ORIGINATOR,
        ProcessApproveRequest.Variables.SUBJECT
      )
    ))
  )

  @Bean
  open fun processVariablesCorrelator() = ProcessVariablesCorrelator(

    // define correlation for every process
    ProcessVariableCorrelation(ProcessApproveRequest.KEY, mapOf(

      // define a correlation for every task needed
      ProcessApproveRequest.Elements.APPROVE_REQUEST to mapOf(
        ProcessApproveRequest.Variables.REQUEST_ID to "io.holunda.camunda.taskpool.example.ApprovalRequest"
      ),

      // and again
      ProcessApproveRequest.Elements.AMEND_REQUEST to mapOf(
        ProcessApproveRequest.Variables.REQUEST_ID to "io.holunda.camunda.taskpool.example.ApprovalRequest"
      )
    ))
  )
}


