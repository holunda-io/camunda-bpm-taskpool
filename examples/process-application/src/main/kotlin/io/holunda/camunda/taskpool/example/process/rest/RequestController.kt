package io.holunda.camunda.taskpool.example.process.rest

import io.holunda.camunda.taskpool.api.business.ProcessingType
import io.holunda.camunda.taskpool.example.process.process.ProcessApproveRequestBean
import io.holunda.camunda.taskpool.example.process.rest.api.RequestApi
import io.holunda.camunda.taskpool.example.process.rest.model.ApprovalRequestDraftDto
import io.holunda.camunda.taskpool.example.process.rest.model.ApprovalRequestDto
import io.holunda.camunda.taskpool.example.process.service.RequestService
import io.holunda.camunda.taskpool.view.auth.UserService
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiParam
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.noContent
import org.springframework.web.bind.annotation.*

@Api("Approval Request Controller", tags = ["Request"])
@RestController
@RequestMapping(path = [Rest.REST_PREFIX])
class RequestController(
  private val processApproveRequestBean: ProcessApproveRequestBean,
  private val requestService: RequestService,
  private val userService: UserService
) : RequestApi {


  override fun startNewApproval(
    @ApiParam(value = "Specifies the id of current user." ,required=true) @RequestHeader(value="X-Current-User-ID", required=true) xCurrentUserID: String,
    @ApiParam("Request to be approved", required=true) @RequestBody request: ApprovalRequestDraftDto
  ): ResponseEntity<Void> {

    val username = userService.getUser(xCurrentUserID).username
    val requestId = requestService.addRequest(draft(request), username)
    processApproveRequestBean.startProcess(requestId, username)

    return noContent().build()
  }

  override fun approveDraft(
    @ApiParam("Request id") @PathVariable("id") id: String,
    @ApiParam(value = "Specifies the id of current user." ,required=true) @RequestHeader(value="X-Current-User-ID", required=true) xCurrentUserID: String
  ): ResponseEntity<Void> {

    val username = userService.getUser(xCurrentUserID).username
    processApproveRequestBean.startProcess(id, username)

    return noContent().build()
  }


  // FIXME, no value, remove this or define in swagger.yaml
  @ApiOperation("Performs approveProcess request task.")
  @PostMapping("/request/{id}/decision/{decision}")
  fun approve(
    @ApiParam("Request id") @PathVariable("id") id: String,
    @ApiParam("Decision of the approver", allowableValues = "APPROVE, REJECT, RETURN", required = true) @PathVariable("decision") decision: String,
    @ApiParam(value = "Specifies the id of current user." ,required=true) @RequestHeader(value="X-Current-User-ID", required=true) xCurrentUserID: String,
    @ApiParam("Comment") @RequestBody comment: String?) {

    val username = userService.getUser(xCurrentUserID).username
    processApproveRequestBean.approveProcess(id, decision, username, comment)
  }

  // FIXME, no value, remove this or define in swagger.yaml
  @ApiOperation("Performs amendProcess request task.")
  @PostMapping("/request/{id}/action/{action}")
  fun amend(
    @ApiParam("Request id") @PathVariable("id") id: String,
    @ApiParam("Decision of the originator", allowableValues = "CANCEL, RESUBMIT", required = true) @PathVariable("action") action: String,
    @ApiParam(value = "Specifies the id of current user." ,required=true) @RequestHeader(value="X-Current-User-ID", required=true) xCurrentUserID: String,
    @ApiParam("Comment") @RequestBody comment: String?
  ) {
    val username = userService.getUser(xCurrentUserID).username
    processApproveRequestBean.amendProcess(id, action, username, comment)
  }

  // FIXME, no value, remove this or define in swagger.yaml
  @ApiOperation("Deletes all process instances.")
  @DeleteMapping("/process")
  fun deleteAllInstances() {
    processApproveRequestBean.deleteAllInstances()
  }

}
