package io.holunda.camunda.taskpool.example.process.rest

import io.holunda.camunda.taskpool.example.process.process.ProcessApproveRequestBean
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiParam
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@Api("Process Starter Controller")
@RestController
open class ProcessStarterController {

  @Autowired
  lateinit var processApproveRequestBean: ProcessApproveRequestBean

  @ApiOperation("Starts process instances up to the given number.")
  @PostMapping("/request/start/{count}")
  open fun startManyInstances(@ApiParam("Total number of instances", defaultValue = "10") @PathVariable("count") count: Int): ResponseEntity<List<String>> {
    val instances = mutableListOf<String>()
    for (numberOfInstances in processApproveRequestBean.countInstances()..count) {
      instances.add(processApproveRequestBean.startProcess().processInstanceId)
    }
    return ResponseEntity.ok(instances)
  }

  @ApiOperation("Deletes all process instances.")
  @DeleteMapping("/request")
  open fun deleteAllInstances() {
    processApproveRequestBean.deleteAllInstances()
  }

  @ApiOperation("Performs approve request task.")
  @PostMapping("/request/{id}/decision/{decision}")
  open fun approve(
    @ApiParam("Process instance id") @PathVariable("id") id: String,
    @ApiParam("Decision of the approver", allowableValues = "APPROVE, REJECT, RETURN", required=true) @PathVariable("decision") decision: String) {
    processApproveRequestBean.approve(id, decision)
  }

  @ApiOperation("Performs amend request task.")
  @PostMapping("/request/{id}/action/{action}")
  open fun amend(
    @ApiParam("Process instance id") @PathVariable("id") id: String,
    @ApiParam("Decision of the originator", allowableValues = "CANCEL, RESUBMIT", required=true) @PathVariable("action") action: String) {
    processApproveRequestBean.amend(id, action)
  }

}
