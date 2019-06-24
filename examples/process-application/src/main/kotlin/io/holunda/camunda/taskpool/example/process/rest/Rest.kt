package io.holunda.camunda.taskpool.example.process.rest

import io.holunda.camunda.taskpool.example.process.rest.model.ApprovalRequestDraftDto
import io.holunda.camunda.taskpool.example.process.rest.model.ApprovalRequestDto
import io.holunda.camunda.taskpool.example.process.rest.model.TaskDto
import io.holunda.camunda.taskpool.example.process.service.Request
import org.camunda.bpm.engine.task.Task
import org.springframework.context.annotation.Configuration
import java.math.BigDecimal
import java.time.OffsetDateTime
import java.time.ZoneId
import java.util.*

class Rest {

  companion object {
    const val REST_PREFIX = "/example-process-approval/rest"
  }

}

/**
 * Converts approval request to DTO.
 */
fun approvalRequestDto(request: Request): ApprovalRequestDto = ApprovalRequestDto()
  .id(request.id)
  .amount(request.amount.toString())
  .applicant(request.applicant)
  .currency(request.currency)
  .subject(request.subject)

/**
 * Converts task to DTO.
 */
fun taskDto(task: Task): TaskDto = TaskDto()
  .id(task.id)
  .assignee(task.assignee)
  .createTime(OffsetDateTime.ofInstant(task.createTime.toInstant(), ZoneId.systemDefault()))
  .description(task.description)
  .formKey(task.formKey)
  .name(task.name)
  .priority(task.priority)
  .apply {
    if (task.dueDate != null) {
      dueDate = OffsetDateTime.ofInstant(task.dueDate.toInstant(), ZoneId.systemDefault())
    }
    if (task.followUpDate != null) {
      followUpDate = OffsetDateTime.ofInstant(task.followUpDate.toInstant(), ZoneId.systemDefault())
    }
  }

/**
 * Converts the DTO to approval request.
 */
fun request(dto: ApprovalRequestDto) = Request(
  id = dto.id,
  amount = BigDecimal(dto.amount),
  currency = dto.currency,
  applicant = dto.applicant,
  subject = dto.subject
)

/**
 * Converts the draft DTO to approval request.
 */
fun draft(dto: ApprovalRequestDraftDto) = Request(
  amount = BigDecimal(dto.amount),
  currency = dto.currency,
  applicant = dto.applicant,
  subject = dto.subject
)
