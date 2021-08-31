package io.holunda.polyflow.example.process.approval.rest

import com.fasterxml.jackson.databind.ObjectMapper
import io.holixon.axon.gateway.query.QueryResponseMessageResponseType
import io.holixon.axon.gateway.query.RevisionQueryParameters
import io.holunda.polyflow.example.process.approval.process.RequestApprovalProcessBean
import io.holunda.polyflow.example.process.approval.rest.api.RequestApi
import io.holunda.polyflow.example.process.approval.rest.model.ApprovalRequestDraftDto
import io.holunda.polyflow.example.process.approval.rest.model.ApprovalRequestDto
import io.holunda.polyflow.example.process.approval.service.Request
import io.holunda.polyflow.example.process.approval.service.RequestService
import io.holunda.polyflow.view.auth.User
import io.holunda.polyflow.view.auth.UserService
import io.holunda.polyflow.view.query.data.DataEntriesForUserQuery
import io.holunda.polyflow.view.query.data.DataEntriesQueryResult
import io.swagger.annotations.Api
import io.swagger.annotations.ApiParam
import org.axonframework.messaging.GenericMessage
import org.axonframework.queryhandling.QueryGateway
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.noContent
import org.springframework.http.ResponseEntity.ok
import org.springframework.web.bind.annotation.*
import java.util.*

@Api(tags = ["Request"])
@RestController
@RequestMapping(path = [Rest.REST_PREFIX])
class RequestController(
  private val requestApprovalProcessBean: RequestApprovalProcessBean,
  private val requestService: RequestService,
  private val userService: UserService,
  private val queryGateway: QueryGateway,
  private val objectMapper: ObjectMapper
) : RequestApi {


  override fun startNewApproval(
    @RequestHeader(value = "X-Current-User-ID", required = true) xCurrentUserID: String,
    @RequestParam(value = "revision", required = false) revision: Optional<String>,
    @RequestBody request: ApprovalRequestDraftDto
  ): ResponseEntity<Void> {

    val revisionNumber = revision.orElseGet { "1" }.toLong()
    val username = userService.getUser(xCurrentUserID).username
    requestApprovalProcessBean.submitDraft(draft(request), username, revisionNumber)

    return noContent().build()
  }


  override fun getApprovalRequest(
    @ApiParam(value = "Specifies the id of current user.", required = true) @RequestHeader(value = "X-Current-User-ID", required = true) xCurrentUserID: String,
    @ApiParam(value = "Request id.", required = true) @PathVariable("id") id: String
  ): ResponseEntity<ApprovalRequestDto> {

    // val username = userService.getUser(xCurrentUserID).username
    val request = requestService.getRequest(id, 1)

    return ok(approvalRequestDto(request))
  }

  override fun getApprovalForUser(
    @RequestHeader(value = "X-Current-User-ID", required = true) xCurrentUserID: String,
    @RequestParam(value = "revision", required = false) revision: Optional<String>
  ): ResponseEntity<List<ApprovalRequestDto>> {

    val revisionNumber = revision.orElse("1").toLong()
    val user = userService.getUser(xCurrentUserID).username

    val result = queryGateway.query(
      GenericMessage.asMessage(
        DataEntriesForUserQuery(
          user = User(user, setOf()),
          page = 1,
          size = Int.MAX_VALUE,
          sort = "",
          filters = listOf()
        )
      ).andMetaData(RevisionQueryParameters(revisionNumber, 10).toMetaData()),
      QueryResponseMessageResponseType.queryResponseMessageResponseType<DataEntriesQueryResult>()
    ).join()

    return ok(result.elements
      .map {
        objectMapper.convertValue(it.payload, Request::class.java)
      }.map {
        approvalRequestDto(it)
      }
    )
  }
}
