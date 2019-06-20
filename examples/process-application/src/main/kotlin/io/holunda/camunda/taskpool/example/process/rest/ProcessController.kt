package io.holunda.camunda.taskpool.example.process.rest

import io.holunda.camunda.taskpool.api.business.ProcessingType
import io.holunda.camunda.taskpool.example.process.process.ProcessApproveRequestBean
import io.holunda.camunda.taskpool.example.process.rest.api.RequestApi
import io.holunda.camunda.taskpool.example.process.service.RequestService
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiParam
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.noContent
import org.springframework.web.bind.annotation.*

@Api("Example Process Controller", tags = ["Request"])
@RestController
@RequestMapping(path = [Rest.REST_PREFIX])
class ProcessController(
  private val processApproveRequestBean: ProcessApproveRequestBean,
  private val requestService: RequestService
) : RequestApi {


  override fun start(
    @ApiParam("Request id") @PathVariable("id") id: String,
    @ApiParam(value = "Specifies the id of current user." ,required=true) @RequestHeader(value="X-Current-User-ID", required=true) xCurrentUserID: String
  ): ResponseEntity<Void> {

    val originator = "kermit" // FIXME, resolve currentUserId

    processApproveRequestBean.startProcess(id, originator)
    requestService.changeRequestState(id, ProcessingType.IN_PROGRESS.of("Submitted"), originator, "New approval request submitted.")
    return noContent().build()
  }

  @ApiOperation("Performs approveProcess request task.")
  @PostMapping("/request/{id}/decision/{decision}")
  fun approve(
    @ApiParam("Request id") @PathVariable("id") id: String,
    @ApiParam("Decision of the approver", allowableValues = "APPROVE, REJECT, RETURN", required = true) @PathVariable("decision") decision: String,
    @ApiParam(value = "Specifies the id of current user." ,required=true) @RequestHeader(value="X-Current-User-ID", required=true) xCurrentUserID: String,
    @ApiParam("Comment") @RequestBody comment: String?) {

    val username = "kermit" // FIXME
    processApproveRequestBean.approveProcess(id, decision, username, comment)
  }

  @ApiOperation("Performs amendProcess request task.")
  @PostMapping("/request/{id}/action/{action}")
  fun amend(
    @ApiParam("Request id") @PathVariable("id") id: String,
    @ApiParam("Decision of the originator", allowableValues = "CANCEL, RESUBMIT", required = true) @PathVariable("action") action: String,
    @ApiParam(value = "Specifies the id of current user." ,required=true) @RequestHeader(value="X-Current-User-ID", required=true) xCurrentUserID: String,
    @ApiParam("Comment") @RequestBody comment: String?
  ) {
    val username = "kermit" // FIXME
    processApproveRequestBean.amendProcess(id, action, username, comment)
  }

  @ApiOperation("Deletes all process instances.")
  @DeleteMapping("/process")
  fun deleteAllInstances() {
    processApproveRequestBean.deleteAllInstances()
  }

}
