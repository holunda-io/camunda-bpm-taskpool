package io.holunda.camunda.taskpool.example.process.service

import io.holunda.camunda.taskpool.api.sender.DataEntryCommandSender
import org.springframework.stereotype.Service
import java.math.BigDecimal


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


  fun addRequest(request: Request) {
    repository.save(request)
    notify(request)
  }

  fun getRequest(id: String): Request {
    return repository.findById(id).orElseThrow { NoSuchElementException("Request with id $id not found.") }
  }

  fun checkRequest(id: String): Boolean = this.repository.existsById(id)

  fun updateRequest(id: String, request: Request) {
    if (checkRequest(id)) {
      this.repository.save(request)
      notify(request)
    }
  }

  fun notify(request: Request) {
    sender.sendDataEntryCommand(
      entryType = BusinessDataEntry.REQUEST,
      entryId = request.id,
      payload = request
    )
  }

  fun getAllRequests(): List<Request> {
    return this.repository.findAll()
  }
}

fun createDummyRequest(id: String) = Request(
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
