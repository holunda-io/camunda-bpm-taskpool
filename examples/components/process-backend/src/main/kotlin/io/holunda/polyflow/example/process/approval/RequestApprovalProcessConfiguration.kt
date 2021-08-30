package io.holunda.polyflow.example.process.approval

import io.holixon.axon.gateway.configuration.query.EnableRevisionAwareQueryGateway
import io.holunda.polyflow.example.process.approval.process.RequestApprovalProcess
import io.holunda.polyflow.example.process.approval.process.RequestApprovalProcess.Variables.AMOUNT
import io.holunda.polyflow.example.process.approval.process.RequestApprovalProcess.Variables.APPLICANT
import io.holunda.polyflow.example.process.approval.process.RequestApprovalProcess.Variables.COMMENT
import io.holunda.polyflow.example.process.approval.process.RequestApprovalProcess.Variables.ORIGINATOR
import io.holunda.polyflow.example.process.approval.process.RequestApprovalProcess.Variables.REQUEST_ID
import io.holunda.polyflow.example.process.approval.service.BusinessDataEntry
import io.holunda.polyflow.example.users.EnableExampleUsers
import io.holunda.polyflow.taskpool.EnableTaskpoolEngineSupport
import io.holunda.polyflow.taskpool.collector.task.enricher.*
import io.holunda.polyflow.taskpool.sender.gateway.CommandErrorHandler
import io.holunda.polyflow.taskpool.sender.gateway.LoggingTaskCommandErrorHandler
import mu.KLogging
import org.axonframework.commandhandling.CommandResultMessage
import org.camunda.bpm.spring.boot.starter.annotation.EnableProcessApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Primary

@Configuration
@EnableProcessApplication
@EnableTaskpoolEngineSupport
@EnableExampleUsers
@EnableRevisionAwareQueryGateway
@Import(RequestApprovalProcessRestConfiguration::class, ProcessApproveRequestSPAConfiguration::class)
class RequestApprovalProcessConfiguration {

  companion object : KLogging()

  @Bean
  fun processVariablesFilter(): ProcessVariablesFilter = ProcessVariablesFilter(
    // define a variable filter for every task inside the process
    TaskVariableFilter(
      RequestApprovalProcess.KEY,
      FilterType.INCLUDE,
      mapOf(
        // define a variable filter for every task
        RequestApprovalProcess.Elements.APPROVE_REQUEST to
          listOf(
            ORIGINATOR.name
          ),
        // and again
        RequestApprovalProcess.Elements.AMEND_REQUEST to
          listOf(
            COMMENT.name,
            APPLICANT.name
          )
      )
    ),
    // define a variable filter for the entire process
    ProcessVariableFilter(
      RequestApprovalProcess.KEY,
      FilterType.INCLUDE,
      listOf(
        REQUEST_ID.name
      )
    ),
    // or even across processes
    ProcessVariableFilter(
      null,
      FilterType.EXCLUDE,
      listOf(
        AMOUNT.name
      )
    )

  )

  @Bean
  fun processVariablesCorrelator(): ProcessVariablesCorrelator = ProcessVariablesCorrelator(

    // define correlation for every process
    ProcessVariableCorrelation(
      RequestApprovalProcess.KEY,
      mapOf(
        // define a correlation for every task needed
        RequestApprovalProcess.Elements.APPROVE_REQUEST to mapOf(
          REQUEST_ID.name to BusinessDataEntry.REQUEST,
          ORIGINATOR.name to BusinessDataEntry.USER
        )
      ),

      // or globally
      mapOf(REQUEST_ID.name to BusinessDataEntry.REQUEST)
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


  @Bean
  @Primary
  fun myTaskCommandErrorHandler(): CommandErrorHandler = object : LoggingTaskCommandErrorHandler(logger) {
    override fun apply(commandMessage: Any, commandResultMessage: CommandResultMessage<out Any?>) {
      logger.error { "<--------- CUSTOM ERROR HANDLER REPORT --------->" }
      super.apply(commandMessage, commandResultMessage)
      logger.error { "<------------------- END ----------------------->" }
    }
  }

  /*

  Uncomment this to override default logging handlers.

  @Bean
  @Primary
  fun myDataEntryCommandSuccessHandler() = object : DataEntryCommandSuccessHandler {
    override fun apply(commandMessage: Any, commandResultMessage: CommandResultMessage<out Any?>) {
      // do something here
      logger.trace { "Success" }
    }
  }

  @Bean
  @Primary
  fun myDataEntryCommandErrorHandler() = object : DataEntryCommandErrorHandler {
    override fun apply(commandMessage: Any, commandResultMessage: CommandResultMessage<out Any?>) {
      // do something here
      logger.error { "Error sending a command: ${commandResultMessage.exceptionResult()}." }
    }
  }
  */

}
