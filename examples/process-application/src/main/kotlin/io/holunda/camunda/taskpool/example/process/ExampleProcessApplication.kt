package io.holunda.camunda.taskpool.example.process

import io.holunda.camunda.datapool.sender.DataEntryCommandErrorHandler
import io.holunda.camunda.datapool.sender.DataEntryCommandSuccessHandler
import io.holunda.camunda.taskpool.EnableTaskpoolEngineSupport
import io.holunda.camunda.taskpool.enricher.FilterType
import io.holunda.camunda.taskpool.enricher.ProcessVariableCorrelation
import io.holunda.camunda.taskpool.enricher.ProcessVariablesCorrelator
import io.holunda.camunda.taskpool.enricher.ProcessVariablesFilter
import io.holunda.camunda.taskpool.enricher.TaskVariableFilter
import io.holunda.camunda.taskpool.example.process.process.ProcessApproveRequest
import io.holunda.camunda.taskpool.example.process.service.BusinessDataEntry
import io.holunda.camunda.taskpool.example.users.EnableExampleUsers
import io.holunda.camunda.taskpool.sender.gateway.LoggingTaskCommandErrorHandler
import io.holunda.camunda.taskpool.sender.gateway.TaskCommandErrorHandler
import mu.KLogging
import org.axonframework.commandhandling.CommandResultMessage
import org.camunda.bpm.spring.boot.starter.annotation.EnableProcessApplication
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.core.annotation.Order


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

  /*
  @Bean
  @Primary
  fun myTaskCommandErrorHandler(): TaskCommandErrorHandler = object : LoggingTaskCommandErrorHandler(logger) {
    override fun apply(commandMessage: Any, commandResultMessage: CommandResultMessage<out Any?>) {
      logger.info { "<--------- CUSTOM ERROR HANDLER REPORT --------->" }
      super.apply(commandMessage, commandResultMessage)
      logger.info { "<------------------- END ----------------------->" }
    }
  }

  @Bean
  @Primary
  fun myDataEntryCommandSuccessHandler() = object : DataEntryCommandSuccessHandler {
    override fun apply(commandMessage: Any, commandResultMessage: CommandResultMessage<out Any?>) {
      // do something here
      logger.info { "Success" }
    }
  }

  @Bean
  @Primary
  fun myDataEntryCommandErrorHandler() = object : DataEntryCommandErrorHandler {
    override fun apply(commandMessage: Any, commandResultMessage: CommandResultMessage<out Any?>) {
      // do something here
      logger.error { "Error" }
    }
  }

  */
}
