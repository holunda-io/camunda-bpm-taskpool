package io.holunda.camunda.taskpool.example.process.service

import io.holunda.camunda.datapool.sender.DataEntryCommandSender
import io.holunda.camunda.taskpool.api.business.DataEntryState
import io.holunda.camunda.taskpool.api.business.Modification
import io.holunda.camunda.taskpool.api.business.ProcessingType
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.OffsetDateTime
import java.util.*
import kotlin.NoSuchElementException


/**
 * Request service acts as an abstraction of an external "legacy" application.
 * It is responsible for stroage of requests and can be used to modify the requests
 * independent from the process.
 */
@Service
class RequestService(
  private val sender: DataEntryCommandSender,
  private val repository: RequestRepository
) {

  fun addRequest(request: Request, username: String): String {
    val saved = repository.save(request)
    changeRequestState(request = request,
      state = ProcessingType.PRELIMINARY.of("Draft"),
      username = username,
      log = "Draft created.",
      logNotes = "Request draft on behalf of ${request.applicant} created.")
    return saved.id
  }

  fun getRequest(id: String): Request {
    return repository.findById(id).orElseThrow { NoSuchElementException("Request with id $id not found.") }
  }

  fun checkRequest(id: String): Boolean = this.repository.existsById(id)

  fun changeRequestState(id: String, state: DataEntryState, username: String, log: String? = null, logNotes: String? = null) =
    changeRequestState(getRequest(id), state, username, log, logNotes)

  fun updateRequest(id: String, request: Request, username: String) {
    if (checkRequest(id)) {
      this.repository.save(request)
      changeRequestState(request, ProcessingType.IN_PROGRESS.of("Amended"), "Request amended.")
    }
  }

  fun getAllRequests(): List<Request> {
    return this.repository.findAll()
  }

  private fun changeRequestState(request: Request, state: DataEntryState, username: String, log: String? = null, logNotes: String? = null) {
    sender.sendDataEntryCommand(
      entryType = BusinessDataEntry.REQUEST,
      entryId = request.id,
      payload = request,
      state = state,
      name = "AR ${request.id}",
      description = request.subject,
      type = "Approval Request",
      modification = Modification(
        time = OffsetDateTime.now(),
        username = username,
        log = log,
        logNotes = logNotes
      ),
      authorizedUsers = listOf(username, request.applicant),
      authorizedGroups = listOf()
    )
  }

}

fun createDummyRequest(id: String = UUID.randomUUID().toString()) = Request(
  id = id,
  subject = "Salary increase",
  amount = BigDecimal(10000),
  currency = "USD",
  applicant = "piggy"
)


object BusinessDataEntry {
  const val REQUEST = "io.holunda.camunda.taskpool.example.ApprovalRequest"
  const val USER = "io.holunda.camunda.taskpool.example.User"
}
