package io.holunda.camunda.taskpool.example.process.rest

import io.holunda.camunda.taskpool.example.process.process.ProcessApproveRequestBean
import io.holunda.camunda.taskpool.example.process.rest.api.ApproveRequestApi
import io.holunda.camunda.taskpool.example.process.rest.model.TaskApproveRequestFormDataDto
import io.holunda.camunda.taskpool.example.process.rest.model.TaskApproveRequestSubmitDataDto
import io.swagger.annotations.ApiParam
import mu.KLogging
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import javax.validation.Valid

@Controller
@RequestMapping(path = [Rest.REST_PREFIX])
class ApproveRequestTaskController(
  private val processApproveRequestBean: ProcessApproveRequestBean
) : ApproveRequestApi {

  companion object : KLogging()

  override fun loadTaskApproveRequestFormData(
    @ApiParam(value = "Task id.", required = true) @PathVariable("id") id: String
  ): ResponseEntity<TaskApproveRequestFormDataDto> {
    logger.info { "Loading data for task $id" }
    val (task, approvalRequest) = processApproveRequestBean.loadApproveTaskFormData(id)
    return ResponseEntity.ok(TaskApproveRequestFormDataDto().approvalRequest(approvalRequestDto(approvalRequest)).task(taskDto(task)))
  }

  override fun submitTaskApproveRequestSubmitData(
    @ApiParam(value = "Task id.", required = true) @PathVariable("id") id: String,
    @ApiParam(value = "Payload to be added to the process instance on task completion.") @Valid @RequestBody payload: TaskApproveRequestSubmitDataDto
  ): ResponseEntity<Void> {
    logger.info { "Submitting data for task $id, $payload" }
    processApproveRequestBean.approveTask(id, payload.decision, payload.comment)
    return ResponseEntity.noContent().build()
  }
}
