package io.holunda.polyflow.example.process.approval.rest

import io.holunda.polyflow.example.process.approval.process.RequestApprovalProcessBean
import io.holunda.polyflow.example.process.approval.rest.api.ApproveRequestApi
import io.holunda.polyflow.example.process.approval.rest.model.TaskApproveRequestFormDataDto
import io.holunda.polyflow.example.process.approval.rest.model.TaskApproveRequestSubmitDataDto
import io.holunda.polyflow.view.auth.UserService
import io.swagger.annotations.Api
import io.swagger.annotations.ApiParam
import mu.KLogging
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import javax.validation.Valid

@Api(tags = ["User Task Approve Request"])
@Controller
@RequestMapping(path = [Rest.REST_PREFIX])
class ApproveRequestTaskController(
  private val requestApprovalProcessBean: RequestApprovalProcessBean,
  private val userService: UserService
) : ApproveRequestApi {

  companion object : KLogging()

  override fun loadTaskApproveRequestFormData(
    @ApiParam(value = "Task id.", required = true) @PathVariable("id") id: String,
    @ApiParam(value = "Specifies the id of current user.", required = true) @RequestHeader(value = "X-Current-User-ID", required = true) xCurrentUserID: String
  ): ResponseEntity<TaskApproveRequestFormDataDto> {

    val username = userService.getUser(xCurrentUserID).username

    logger.debug { "Loading data task $id for user $username" }
    val (task, approvalRequest) = requestApprovalProcessBean.loadApproveTaskFormData(id)
    return ResponseEntity.ok(TaskApproveRequestFormDataDto().approvalRequest(approvalRequestDto(approvalRequest)).task(taskDto(task)))
  }

  @Transactional
  override fun submitTaskApproveRequestSubmitData(
    @ApiParam(value = "Task id.", required = true) @PathVariable("id") id: String,
    @ApiParam(value = "Specifies the id of current user.", required = true) @RequestHeader(value = "X-Current-User-ID", required = true) xCurrentUserID: String,
    @ApiParam(value = "Payload to be added to the process instance on task completion.") @Valid @RequestBody payload: TaskApproveRequestSubmitDataDto
  ): ResponseEntity<Void> {

    val username = userService.getUser(xCurrentUserID).username

    logger.debug { "User $username is submitting data for task $id, $payload" }
    requestApprovalProcessBean.approveTask(id, payload.decision, username, payload.comment)
    return ResponseEntity.noContent().build()
  }
}
