package io.holunda.camunda.taskpool.example.process

import io.holunda.camunda.taskpool.EnableTaskpoolEngineSupport
import io.holunda.camunda.taskpool.enricher.FilterType
import io.holunda.camunda.taskpool.enricher.ProcessVariableCorrelation
import io.holunda.camunda.taskpool.enricher.ProcessVariablesCorrelator
import io.holunda.camunda.taskpool.enricher.ProcessVariablesFilter
import io.holunda.camunda.taskpool.enricher.TaskVariableFilter
import io.holunda.camunda.taskpool.example.process.process.ProcessApproveRequest
import io.holunda.camunda.taskpool.example.process.service.BusinessDataEntry
import io.holunda.camunda.taskpool.example.users.EnableExampleUsers
import mu.KLogging
import org.camunda.bpm.spring.boot.starter.annotation.EnableProcessApplication
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean


fun main(args: Array<String>) {
  SpringApplication.run(ExampleProcessApplication::class.java, *args)
}

@SpringBootApplication
@EnableProcessApplication
@EnableTaskpoolEngineSupport
@EnableExampleUsers
class ExampleProcessApplication {

  companion object : KLogging()

  @Bean
  fun processVariablesFilter(): ProcessVariablesFilter = ProcessVariablesFilter(

    // define a variable filter for every process
    TaskVariableFilter(
      ProcessApproveRequest.KEY,
      FilterType.INCLUDE,
      mapOf(

        // define a variable filter for every task
        ProcessApproveRequest.Elements.APPROVE_REQUEST to
          listOf(
            ProcessApproveRequest.Variables.REQUEST_ID,
            ProcessApproveRequest.Variables.ORIGINATOR
          ),

        // and again
        ProcessApproveRequest.Elements.AMEND_REQUEST to
          listOf(
            ProcessApproveRequest.Variables.REQUEST_ID,
            ProcessApproveRequest.Variables.COMMENT,
            ProcessApproveRequest.Variables.APPLICANT
          )
      ))
  )

  @Bean
  fun processVariablesCorrelator(): ProcessVariablesCorrelator = ProcessVariablesCorrelator(

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

  /*
  Alternative example
  @Bean
  fun requestProjection(properties: DataEntrySenderProperties): DataEntryProjectionSupplier
    = dataEntrySupplier(entryType = BusinessDataEntry.REQUEST,
    projectionFunction = BiFunction { id, payload ->
      DataEntry(
        entryType = BusinessDataEntry.REQUEST,
        entryId = id,
        applicationName = properties.applicationName,
        payload = serialize(payload),
        type = "Approval Request",
        name = "AR $id"
      )
    })
  */

}
