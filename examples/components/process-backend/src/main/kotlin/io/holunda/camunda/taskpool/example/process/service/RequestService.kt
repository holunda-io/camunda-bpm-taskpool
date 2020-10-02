package io.holunda.camunda.taskpool.example.process.service

import io.holunda.camunda.datapool.sender.DataEntryCommandSender
import io.holunda.camunda.taskpool.api.business.AuthorizationChange.Companion.addUser
import io.holunda.camunda.taskpool.api.business.DataEntryState
import io.holunda.camunda.taskpool.api.business.Modification
import io.holunda.camunda.taskpool.api.business.ProcessingType
import io.holunda.camunda.taskpool.view.query.RevisionValue
import org.axonframework.messaging.MetaData
import org.axonframework.serialization.Revision
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.OffsetDateTime
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import kotlin.NoSuchElementException


/**
 * Request service acts as an abstraction of an external "legacy" application.
 * It is responsible for storage of requests and can be used to modify the requests
 * independent from the process.
 */
@Service
class RequestService(
  private val sender: DataEntryCommandSender,
  private val repository: RequestRepository
) {

  fun addRequest(request: Request, username: String, revision: Long): String {
    val saved = repository.save(request)
    sender.sendDataEntryCommand(
      entryType = BusinessDataEntry.REQUEST,
      entryId = request.id,
      payload = request,
      state = ProcessingType.PRELIMINARY.of("Draft"),
      name = "AR ${request.id}",
      description = request.subject,
      type = "Approval Request",
      modification = Modification(
        time = OffsetDateTime.now(),
        username = username,
        log = "Draft created.",
        logNotes = "Request draft on behalf of ${request.applicant} created."
      ),
      authorizations = listOf(addUser(username), addUser(request.applicant)),
      metaData = RevisionValue(revision).toMetaData()
    )
    return saved.id
  }

  fun getRequest(id: String): Request {
    return repository.findById(id).orElseThrow { NoSuchElementException("Request with id $id not found.") }
  }

  fun checkRequest(id: String): Boolean = this.repository.existsById(id)


  fun updateRequest(id: String, request: Request, username: String, revision: Long) {
    if (checkRequest(id)) {
      this.repository.save(request)
      changeRequestState(request, ProcessingType.IN_PROGRESS.of(state = "Amended"), username = username, revision = revision, log = "Request amended.")
    }
  }

  fun getAllRequests(): List<Request> {
    return this.repository.findAll()
  }

  fun changeRequestState(id: String, state: DataEntryState, username: String, revision: Long, log: String? = null, logNotes: String? = null) =
    changeRequestState(getRequest(id), state, username, revision, log, logNotes)


  private fun changeRequestState(request: Request, state: DataEntryState, username: String, revision: Long, log: String? = null, logNotes: String? = null) {
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
      authorizations = listOf(addUser(username)),
      metaData = RevisionValue(revision).toMetaData()
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
