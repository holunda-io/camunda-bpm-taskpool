package io.holunda.polyflow.example.process.approval.service

import com.fasterxml.jackson.databind.ObjectMapper
import io.holixon.axon.gateway.query.RevisionQueryParameters
import io.holixon.axon.gateway.query.RevisionValue
import io.holunda.camunda.taskpool.api.business.AuthorizationChange.Companion.addUser
import io.holunda.camunda.taskpool.api.business.Modification
import io.holunda.camunda.taskpool.api.business.ProcessingType
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.OffsetDateTime
import java.util.*


/**
 * Request service acts as an abstraction of an external "legacy" application.
 * It is responsible for storage of requests and can be used to modify the requests
 * independent of the process.
 */
@Service
class RequestService(
  private val dataEntryRepository: RequestDataEntryRepository,
  private val objectMapper: ObjectMapper
) {

  fun addRequest(request: Request, username: String, revision: Long): Long {
    dataEntryRepository.save(
      entryType = BusinessDataEntry.REQUEST,
      entryId = request.id,
      payload = request,
      state = ProcessingType.PRELIMINARY.of("Draft"),
      name = request.name(),
      description = request.description(),
      type = request.type(),
      modification = Modification(
        time = OffsetDateTime.now(),
        username = username,
        log = "Draft created.",
        logNotes = "Request draft on behalf of ${request.applicant} created."
      ),
      authorizationChanges = listOf(addUser(username), addUser(request.applicant)),
      metaData = RevisionValue(revision).toMetaData()
    )
    checkRequest(request.id, revision)
    return revision
  }

  fun getRequest(id: String, revision: Long): Request {
    return getAllRequests(revision).find { it.id == id } ?: throw NoSuchElementException("Request with id $id not found.")
  }

  fun checkRequest(id: String, revision: Long): Boolean {
    return getAllRequests(revision).find { it.id == id } != null
  }


  fun updateRequest(id: String, request: Request, username: String, revision: Long): Long {
    if (checkRequest(id, revision)) {
      val newRevision = revision + 1
      dataEntryRepository.save(
        entryType = BusinessDataEntry.REQUEST,
        entryId = request.id,
        payload = request,
        state = ProcessingType.IN_PROGRESS.of("Amended"),
        name = request.name(),
        description = request.description(),
        type = request.type(),
        modification = Modification(
          time = OffsetDateTime.now(),
          username = username,
          log = "Approval request amended.",
          logNotes = "Request on behalf of ${request.applicant} amended."
        ),
        authorizationChanges = listOf(addUser(username), addUser(request.applicant)),
        metaData = RevisionValue(newRevision).toMetaData()
      )
      return newRevision
    }
    return revision
  }

  fun getAllRequests(revision: Long): List<Request> {
    return dataEntryRepository
      .getAll(RevisionQueryParameters(minimalRevision = revision))
      .map { objectMapper.convertValue(it.payload, Request::class.java) }
  }

  /**
   * Retrieves all request in draft state.
   */
  fun getAllDraftRequests(revision: Long): List<Request> {
    return dataEntryRepository
      .getAll(RevisionQueryParameters(minimalRevision = revision))
      .filter { it.state.processingType == ProcessingType.PRELIMINARY }
      .map { objectMapper.convertValue(it.payload, Request::class.java) }
  }

}

fun createSalaryRequest(id: String = UUID.randomUUID().toString()) = Request(
  id = id,
  subject = "Salary increase",
  amount = BigDecimal(10000),
  currency = "USD",
  applicant = "piggy"
)

fun createAdvertisingCampaignRequest(id: String = UUID.randomUUID().toString()) = Request(
  id = id,
  subject = "Small advertising campaign",
  amount = BigDecimal(60000),
  currency = "USD",
  applicant = "ironman"
)


object BusinessDataEntry {
  const val REQUEST = "io.holunda.camunda.taskpool.example.ApprovalRequest"
  const val USER = "io.holunda.camunda.taskpool.example.User"
}

