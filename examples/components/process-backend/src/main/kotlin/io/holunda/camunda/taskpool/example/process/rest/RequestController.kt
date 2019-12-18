package io.holunda.camunda.taskpool.example.process.rest

import io.holunda.camunda.taskpool.example.process.process.RequestApprovalProcessBean
import io.holunda.camunda.taskpool.example.process.rest.api.RequestApi
import io.holunda.camunda.taskpool.example.process.rest.model.ApprovalRequestDraftDto
import io.holunda.camunda.taskpool.example.process.rest.model.ApprovalRequestDto
import io.holunda.camunda.taskpool.example.process.service.RequestService
import io.holunda.camunda.taskpool.view.auth.UserService
import io.swagger.annotations.Api
import io.swagger.annotations.ApiParam
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.noContent
import org.springframework.http.ResponseEntity.ok
import org.springframework.web.bind.annotation.*

@Api(tags = ["Request"])
@RestController
@RequestMapping(path = [Rest.REST_PREFIX])
class RequestController(
  private val requestApprovalProcessBean: RequestApprovalProcessBean,
  private val requestService: RequestService,
  private val userService: UserService
) : RequestApi {


  override fun startNewApproval(
    @ApiParam(value = "Specifies the id of current user." ,required=true) @RequestHeader(value="X-Current-User-ID", required=true) xCurrentUserID: String,
    @ApiParam("Request to be approved", required=true) @RequestBody request: ApprovalRequestDraftDto
  ): ResponseEntity<Void> {

    val username = userService.getUser(xCurrentUserID).username
    val requestId = requestService.addRequest(draft(request), username)
    requestApprovalProcessBean.startProcess(requestId, username)

    return noContent().build()
  }


  override fun getApprovalRequest(
    @ApiParam(value = "Specifies the id of current user." ,required=true) @RequestHeader(value="X-Current-User-ID", required=true) xCurrentUserID: String,
    @ApiParam(value = "Request id.", required = true) @PathVariable("id") id: String
  ): ResponseEntity<ApprovalRequestDto> {

    // val username = userService.getUser(xCurrentUserID).username
    val request = requestService.getRequest(id)

    return ok(approvalRequestDto(request))
  }
}
