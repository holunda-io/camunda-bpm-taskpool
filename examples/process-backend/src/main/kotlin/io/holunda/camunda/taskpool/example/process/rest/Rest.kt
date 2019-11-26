package io.holunda.camunda.taskpool.example.process.rest

import io.holunda.camunda.taskpool.example.process.rest.model.ApprovalRequestDraftDto
import io.holunda.camunda.taskpool.example.process.rest.model.ApprovalRequestDto
import io.holunda.camunda.taskpool.example.process.rest.model.TaskDto
import io.holunda.camunda.taskpool.example.process.service.Request
import io.holunda.camunda.taskpool.view.auth.UnknownUserException
import org.camunda.bpm.engine.task.Task
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import java.math.BigDecimal
import java.time.OffsetDateTime
import java.time.ZoneId
import java.util.*

class Rest {

  companion object {
    const val REST_PREFIX = "/example-process-approval/rest"
  }
}


@Configuration
@ControllerAdvice
class RestExceptionConfiguration {

  @ResponseStatus(value = HttpStatus.FORBIDDEN, reason = "Unknown user.")
  @ExceptionHandler(value = [UnknownUserException::class])
  fun forbiddenException() = Unit

  @ResponseStatus(value = HttpStatus.NOT_FOUND, reason ="Element not found.")
  @ExceptionHandler(value = [NoSuchElementException::class])
  fun notFoundException() = Unit
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
