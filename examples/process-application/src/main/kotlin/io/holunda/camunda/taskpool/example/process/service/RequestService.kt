package io.holunda.camunda.taskpool.example.process.service

import io.holunda.camunda.taskpool.api.sender.DataEntryCommandSender
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.util.concurrent.ConcurrentHashMap


/**
 * Request service acts as an abstraction of an external "legacy" application.
 * It is responsible for stroage of requests and can be used to modify the requests
 * independent from the process.
 */
@Service
class RequestService(
  private val sender: DataEntryCommandSender
) {

  private val requestStorage: MutableMap<String, Request> = ConcurrentHashMap()

  fun addRequest(id: String, request: Request) {
    this.requestStorage[id] = request
    eventModification(request)
  }

  fun getRequest(id: String): Request? {
    return this.requestStorage[id]
  }

  fun checkRequest(id: String): Boolean = this.requestStorage.containsKey(id)

  fun updateRequest(id: String, request: Request) {
    if (checkRequest(id)) {
      this.requestStorage[id] = request
      eventModification(request)
    }
  }

  private fun eventModification(request: Request) {
    sender.sendDataEntryCommand(
      entryType = BusinessDataEntry.REQUEST,
      entryId = request.id,
      payload = request
    )
  }
}

fun createDummyRequest(id: String) = Request(
  id = id,
  subject = "Salary increase",
  amount = BigDecimal(10000),
  currency = "USD",
  applicant = "piggy",
  originator = "kermit"
)


data class Request(
  val id: String,
  val originator: String,
  val applicant: String,
  val subject: String,
  val amount: BigDecimal,
  val currency: String
)

object BusinessDataEntry {
  const val REQUEST = "io.holunda.camunda.taskpool.example.ApprovalRequest"
}
