package io.holunda.camunda.taskpool.example.process.rest

import io.holunda.camunda.taskpool.example.process.process.ProcessApproveRequestBean
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiParam
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@Api("Process Controller")
@RestController
open class ProcessController {

  @Autowired
  lateinit var processApproveRequestBean: ProcessApproveRequestBean

  @ApiOperation("Performs approve request task.")
  @PostMapping("/request/{id}/decision/{decision}")
  open fun approve(
    @ApiParam("Request id") @PathVariable("id") id: String,
    @ApiParam("Decision of the approver", allowableValues = "APPROVE, REJECT, RETURN", required = true) @PathVariable("decision") decision: String,
    @ApiParam("Comment") @RequestBody comment: String?) {
    processApproveRequestBean.approve(id, decision, comment)
  }

  @ApiOperation("Performs amend request task.")
  @PostMapping("/request/{id}/action/{action}")
  open fun amend(
    @ApiParam("Request id") @PathVariable("id") id: String,
    @ApiParam("Decision of the originator", allowableValues = "CANCEL, RESUBMIT", required = true) @PathVariable("action") action: String) {
    processApproveRequestBean.amend(id, action)
  }

  @ApiOperation("Deletes all process instances.")
  @DeleteMapping("/process")
  open fun deleteAllInstances() {
    processApproveRequestBean.deleteAllInstances()
  }

}
