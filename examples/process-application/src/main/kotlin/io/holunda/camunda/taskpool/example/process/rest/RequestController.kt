package io.holunda.camunda.taskpool.example.process.rest

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
  private val requestService: RequestService
) {

  @ApiOperation("Submits a new request.")
  @PostMapping("/request/")
  fun submitRequest(): ResponseEntity<String> {
    val requestId = "AR-${UUID.randomUUID()}"
    requestService.addRequest(createDummyRequest(requestId))
    return ResponseEntity.ok(requestId)
  }

  @ApiOperation("Retrieves a request by id.")
  @GetMapping("/request/{id}")
  fun getRequestById(@ApiParam("id") @PathVariable("id") id: String): ResponseEntity<Request> {
    val request = requestService.getRequest(id)
    return ResponseEntity.ok(request)
  }

  @ApiOperation("Retrieves all requests.")
  @GetMapping("/requests")
  fun getRequests(): ResponseEntity<List<Request>> {
    val requests = requestService.getAllRequests()
    return ResponseEntity.ok(requests)
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
