package io.holunda.camunda.taskpool.example.process.rest

import io.holunda.camunda.taskpool.example.process.process.ProcessApproveRequestBean
import io.holunda.camunda.taskpool.example.process.rest.api.AmendRequestApi
import io.holunda.camunda.taskpool.example.process.rest.model.TaskAmendRequestFormDataDto
import io.holunda.camunda.taskpool.example.process.rest.model.TaskAmendRequestSubmitDataDto
import io.swagger.annotations.ApiParam
import mu.KLogging
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import javax.validation.Valid

@Controller
@RequestMapping(path = [RestConfiguration.REST_PREFIX])
class AmendRequestTaskController(
  private val processApproveRequestBean: ProcessApproveRequestBean
) : AmendRequestApi {

  companion object : KLogging()

  override fun loadTaskAmendRequestFormData(
    @ApiParam(value = "Task id.", required = true) @PathVariable("id") id: String
  ): ResponseEntity<TaskAmendRequestFormDataDto> {
    logger.info { "Loading data for task $id" }
    val (task, approvalRequest) = processApproveRequestBean.loadAmendTaskFormData(id)
    return ResponseEntity.ok(TaskAmendRequestFormDataDto().approvalRequest(approvalRequestDto(approvalRequest)).task(taskDto(task)))
  }

  override fun submitTaskAmendRequestSubmitData(
    @ApiParam(value = "Task id.", required = true) @PathVariable("id") id: String,
    @ApiParam(value = "Payload to be added to the process instance on task completion.") @Valid @RequestBody payload: TaskAmendRequestSubmitDataDto
  ): ResponseEntity<Void> {
    logger.info { "Submitting data for task $id, $payload" }
    processApproveRequestBean.amendTask(id, payload.action, request(payload.approvalRequest), payload.comment)
    return ResponseEntity.noContent().build()
  }
}
