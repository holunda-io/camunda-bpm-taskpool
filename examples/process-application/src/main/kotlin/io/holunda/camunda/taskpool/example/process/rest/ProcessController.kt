package io.holunda.camunda.taskpool.example.process.rest

import io.holunda.camunda.taskpool.example.process.process.ProcessApproveRequestBean
import io.holunda.camunda.taskpool.example.process.rest.api.RequestApi
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiParam
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.noContent
import org.springframework.web.bind.annotation.*

@Api("Example Process Controller", tags = ["Request"])
@RestController
@RequestMapping(path = [Rest.REST_PREFIX])
open class ProcessController : RequestApi {

  @Autowired
  lateinit var processApproveRequestBean: ProcessApproveRequestBean

  override fun start(
    @ApiParam("Request id") @PathVariable("id") id: String,
    @ApiParam(value = "Originator") @PathVariable("originator") originator: String
  ): ResponseEntity<Void> {
    processApproveRequestBean.startProcess(id, originator)
    return noContent().build()
  }

  @ApiOperation("Performs approveProcess request task.")
  @PostMapping("/request/{id}/decision/{decision}")
  open fun approve(
    @ApiParam("Request id") @PathVariable("id") id: String,
    @ApiParam("Decision of the approver", allowableValues = "APPROVE, REJECT, RETURN", required = true) @PathVariable("decision") decision: String,
    @ApiParam("Comment") @RequestBody comment: String?) {
    processApproveRequestBean.approveProcess(id, decision, comment)
  }

  @ApiOperation("Performs amendProcess request task.")
  @PostMapping("/request/{id}/action/{action}")
  open fun amend(
    @ApiParam("Request id") @PathVariable("id") id: String,
    @ApiParam("Decision of the originator", allowableValues = "CANCEL, RESUBMIT", required = true) @PathVariable("action") action: String,
    @ApiParam("Comment") @RequestBody comment: String?
  ) {
    processApproveRequestBean.amendProcess(id, action, comment)
  }

  @ApiOperation("Deletes all process instances.")
  @DeleteMapping("/process")
  open fun deleteAllInstances() {
    processApproveRequestBean.deleteAllInstances()
  }

}
