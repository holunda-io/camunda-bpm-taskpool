package io.holunda.polyflow.example.process.approval.rest

import io.holunda.polyflow.example.process.approval.rest.model.ApprovalRequestDraftDto
import io.holunda.polyflow.example.process.approval.rest.model.ApprovalRequestDto
import io.holunda.polyflow.example.process.approval.rest.model.TaskDto
import io.holunda.polyflow.example.process.approval.service.Request
import org.camunda.bpm.engine.task.Task
import java.math.BigDecimal
import java.time.OffsetDateTime
import java.time.ZoneId

/**
 * REST constants.
 */
object Rest {
  const val REST_PREFIX = "/example-process-approval/rest"
}

/**
 * Converts approval request to DTO.
 */
fun approvalRequestDto(request: Request): ApprovalRequestDto = ApprovalRequestDto()
  .id(request.id)
  .amount(request.amount.toFloat())
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
  amount = BigDecimal(dto.amount.toString()),
  currency = dto.currency,
  applicant = dto.applicant,
  subject = dto.subject
)

/**
 * Converts the draft DTO to approval request.
 */
fun draft(dto: ApprovalRequestDraftDto) = Request(
  amount = BigDecimal(dto.amount.toString()),
  currency = dto.currency,
  applicant = dto.applicant,
  subject = dto.subject
)
