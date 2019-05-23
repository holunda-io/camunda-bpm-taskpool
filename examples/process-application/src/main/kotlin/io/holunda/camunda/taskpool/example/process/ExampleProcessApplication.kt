package io.holunda.camunda.taskpool.example.process

import io.holunda.camunda.datapool.DataEntrySenderProperties
import io.holunda.camunda.datapool.projector.DataEntryProjectionSupplier
import io.holunda.camunda.datapool.projector.dataEntrySupplier
import io.holunda.camunda.taskpool.EnableTaskpoolEngineSupport
import io.holunda.camunda.taskpool.api.business.DataEntry
import io.holunda.camunda.taskpool.api.business.EntryId
import io.holunda.camunda.taskpool.api.business.Modification
import io.holunda.camunda.taskpool.enricher.*
import io.holunda.camunda.taskpool.example.process.process.ProcessApproveRequest
import io.holunda.camunda.taskpool.example.process.service.BusinessDataEntry
import io.holunda.camunda.taskpool.example.process.service.Request
import io.holunda.camunda.taskpool.example.process.service.SimpleUserService
import io.holunda.camunda.variable.serializer.serialize
import mu.KLogging
import org.camunda.bpm.spring.boot.starter.annotation.EnableProcessApplication
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component
import java.time.OffsetDateTime
import java.util.function.BiFunction


fun main(args: Array<String>) {
  SpringApplication.run(ExampleProcessApplication::class.java, *args)
}

@SpringBootApplication
@EnableProcessApplication
@EnableTaskpoolEngineSupport
class ExampleProcessApplication {

  companion object : KLogging()

  @Bean
  fun processVariablesFilter(): ProcessVariablesFilter = ProcessVariablesFilter(

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

  @Bean
  fun requestProjection(properties: DataEntrySenderProperties): DataEntryProjectionSupplier
    = dataEntrySupplier(entryType = Request::javaClass.name,
    projectionFunction = BiFunction { id, payload ->
      DataEntry(
        entryType = Request::javaClass.name,
        entryId = id,
        applicationName = properties.applicationName,
        payload = serialize(payload),
        type = "Approval Request",
        name = "AR $id",
        modification = Modification(OffsetDateTime.now())
      )
    })


  @Bean
  fun registerUsers(simpleUserService: SimpleUserService): ApplicationRunner {
    return ApplicationRunner {
      val users = simpleUserService.getAllUsers()
      users.forEach {
        simpleUserService.notify(it)
      }
    }
  }
}

@Component
class UserProjectionSupplier(private val properties: DataEntrySenderProperties) : DataEntryProjectionSupplier {
  override val entryType = SimpleUserService.RichUserObject::javaClass.name
  override fun get(): BiFunction<EntryId, Any, DataEntry> = BiFunction { id, payload ->
    payload as SimpleUserService.RichUserObject
    DataEntry(
      entryType = this.entryType,
      entryId = id,
      applicationName = properties.applicationName,
      payload = serialize(payload),
      type = "User",
      name = payload.username
    )
  }
}


