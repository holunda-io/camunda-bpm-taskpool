package io.holunda.camunda.taskpool.example.process

import io.holunda.camunda.taskpool.EnableTaskpoolEngineSupport
import io.holunda.camunda.taskpool.enricher.*
import io.holunda.camunda.taskpool.example.process.process.ProcessApproveRequest
import io.holunda.camunda.taskpool.example.process.service.BusinessDataEntry
import io.holunda.camunda.taskpool.example.process.service.SimpleUserService
import mu.KLogging
import org.camunda.bpm.spring.boot.starter.annotation.EnableProcessApplication
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean


fun main(args: Array<String>) {
  SpringApplication.run(ExampleProcessApplication::class.java, *args)
}

@SpringBootApplication
@EnableProcessApplication
@EnableTaskpoolEngineSupport
open class ExampleProcessApplication {

  companion object : KLogging()

  @Bean
  open fun processVariablesFilter(): ProcessVariablesFilter = ProcessVariablesFilter(

    // define a applyFilter for every process
    ProcessVariableFilter(
      ProcessApproveRequest.KEY,
      FilterType.INCLUDE,
      mapOf(

        // define a applyFilter for every task
        ProcessApproveRequest.Elements.APPROVE_REQUEST to
          listOf(
            ProcessApproveRequest.Variables.REQUEST_ID,
            ProcessApproveRequest.Variables.ORIGINATOR
          ),

        // and again
        ProcessApproveRequest.Elements.AMEND_REQUEST to
          listOf(
            ProcessApproveRequest.Variables.REQUEST_ID,
            ProcessApproveRequest.Variables.COMMENT
          )
      ))
  )

  @Bean
  open fun processVariablesCorrelator(): ProcessVariablesCorrelator = ProcessVariablesCorrelator(

    // define correlation for every process
    ProcessVariableCorrelation(ProcessApproveRequest.KEY,
      mapOf(
        // define a correlation for every task needed
        ProcessApproveRequest.Elements.APPROVE_REQUEST to mapOf(
          ProcessApproveRequest.Variables.REQUEST_ID to BusinessDataEntry.REQUEST,
          ProcessApproveRequest.Variables.ORIGINATOR to BusinessDataEntry.USER
        )
      ),
      // or globally
      mapOf(ProcessApproveRequest.Variables.REQUEST_ID to BusinessDataEntry.REQUEST)
    )
  )

  @Bean
  open fun registerUsers(simpleUserService: SimpleUserService): ApplicationRunner {
    return ApplicationRunner {
      val users = simpleUserService.getAllUsers()
      users.forEach {
        simpleUserService.notify(it)
      }
    }
  }

}


