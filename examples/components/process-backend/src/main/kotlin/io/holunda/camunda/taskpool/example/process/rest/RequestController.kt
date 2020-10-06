package io.holunda.camunda.taskpool.example.process.rest

import com.fasterxml.jackson.databind.ObjectMapper
import io.holixon.axon.gateway.query.QueryResponseMessageResponseType
import io.holixon.axon.gateway.query.RevisionQueryParameters
import io.holunda.camunda.taskpool.example.process.process.RequestApprovalProcessBean
import io.holunda.camunda.taskpool.example.process.rest.api.RequestApi
import io.holunda.camunda.taskpool.example.process.rest.model.ApprovalRequestDraftDto
import io.holunda.camunda.taskpool.example.process.rest.model.ApprovalRequestDto
import io.holunda.camunda.taskpool.example.process.service.Request
import io.holunda.camunda.taskpool.example.process.service.RequestService
import io.holunda.camunda.taskpool.view.auth.User
import io.holunda.camunda.taskpool.view.auth.UserService
import io.holunda.camunda.taskpool.view.query.data.DataEntriesForUserQuery
import io.holunda.camunda.taskpool.view.query.data.DataEntriesQueryResult
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
  private val queryGateway: QueryGateway, // FIXME -> move
  private val objectMapper: ObjectMapper
) : RequestApi {


  override fun startNewApproval(
    @ApiParam(value = "Specifies the id of current user.", required = true) @RequestHeader(value = "X-Current-User-ID", required = true) xCurrentUserID: String,
    @ApiParam("Request to be approved", required = true) @RequestBody request: ApprovalRequestDraftDto
  ): ResponseEntity<Void> {

    val revision = 1L
    val username = userService.getUser(xCurrentUserID).username
    requestApprovalProcessBean.submitDraft(draft(request), username, revision)

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
    @ApiParam(value = "Specifies the id of current user.", required = true) @RequestHeader(value = "X-Current-User-ID", required = true) xCurrentUserID: String,
    @ApiParam(value = "Revision of the projection.") @RequestParam(value = "revision", required = false) revision: Optional<String>): ResponseEntity<List<ApprovalRequestDto>> {

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
