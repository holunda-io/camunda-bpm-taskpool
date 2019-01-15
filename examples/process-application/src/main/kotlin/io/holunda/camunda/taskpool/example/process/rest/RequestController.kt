package io.holunda.camunda.taskpool.example.process.rest

import io.holunda.camunda.taskpool.example.process.process.ProcessApproveRequestBean
import io.holunda.camunda.taskpool.example.process.service.Request
import io.holunda.camunda.taskpool.example.process.service.RequestService
import io.holunda.camunda.taskpool.example.process.service.createDummyRequest
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiParam
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

@Api("Request Controller", tags = ["Request Controller"])
@RestController
@RequestMapping(path = [Rest.REST_PREFIX])
class RequestController(
  private val requestService: RequestService,
  private val processApproveRequestBean: ProcessApproveRequestBean
) {

  @ApiOperation("Submits a new request and starts the approval process.")
  @PostMapping("/request")
  fun submitRequest(): ResponseEntity<String> {

    val requestId = "AR-${UUID.randomUUID()}"
    requestService.addRequest(requestId, createDummyRequest(requestId))
    processApproveRequestBean.startProcess(requestId)
    return ResponseEntity.ok(requestId)
  }

  @ApiOperation("Retrieves a request by id.")
  @GetMapping("/request/{id}")
  fun getRequestById(@ApiParam("id") @PathVariable("id") id: String): ResponseEntity<Request> {

    val request = requestService.getRequest(id)
    return if (request != null) {
      ResponseEntity.ok(request)
    } else {
      ResponseEntity.notFound().build()
    }
  }

  @ApiOperation("Updates a request by id.")
  @PostMapping("/request/{id}")
  fun updateRequestById(@ApiParam("id") @PathVariable("id") id: String, @RequestBody request: Request): ResponseEntity<Void> {

    return if (requestService.checkRequest(id)) {
      requestService.updateRequest(id, request)
      ResponseEntity.noContent().build()
    } else {
      ResponseEntity.notFound().build()
    }
  }
}
