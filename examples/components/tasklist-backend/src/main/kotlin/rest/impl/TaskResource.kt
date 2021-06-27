package io.holunda.camunda.taskpool.example.tasklist.rest.impl

import io.holunda.camunda.taskpool.api.task.*
import io.holunda.camunda.taskpool.example.tasklist.auth.CurrentUserService
import io.holunda.camunda.taskpool.example.tasklist.rest.ElementNotFoundException
import io.holunda.camunda.taskpool.example.tasklist.rest.Rest
import io.holunda.camunda.taskpool.example.tasklist.rest.api.TaskApi
import io.holunda.polyflow.view.Task
import io.holunda.polyflow.view.auth.UserService
import io.holunda.polyflow.view.query.task.TaskForIdQuery
import io.swagger.annotations.Api
import mu.KLogging
import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.queryhandling.QueryGateway
import org.camunda.bpm.engine.variable.Variables
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.OffsetDateTime
import java.util.*
import javax.validation.Valid
import javax.validation.constraints.NotNull


@RestController
@CrossOrigin
@RequestMapping(Rest.REQUEST_PATH)
class TaskResource(
  private val gateway: CommandGateway,
  private val queryGateway: QueryGateway,
  private val currentUserService: CurrentUserService,
  private val userService: UserService
) : TaskApi {

  companion object : KLogging()

  override fun claim(
    @PathVariable("id") id: String,
    @RequestHeader(value = "X-Current-User-ID", required = false) xCurrentUserID: Optional<String>
  ): ResponseEntity<Void> {

    val task = getTask(id)
    val userIdentifier = currentUserService.getCurrentUser()
    val user = userService.getUser(userIdentifier)

    send(
      ClaimInteractionTaskCommand(
        id = task.id,
        sourceReference = task.sourceReference,
        taskDefinitionKey = task.taskDefinitionKey,
        assignee = user.username
      )
    )

    return ResponseEntity.noContent().build()
  }

  override fun unclaim(
    @PathVariable("id") id: String,
    @RequestHeader(value = "X-Current-User-ID", required = false) xCurrentUserID: Optional<String>
  ): ResponseEntity<Void> {

    val task = getTask(id)

    send(
      UnclaimInteractionTaskCommand(
        id = task.id,
        sourceReference = task.sourceReference,
        taskDefinitionKey = task.taskDefinitionKey
      )
    )

    return ResponseEntity.noContent().build()
  }

  override fun complete(
    @PathVariable("id") id: String,
    @RequestHeader(value = "X-Current-User-ID", required = false) xCurrentUserID: Optional<String>,
    @Valid @RequestBody @NotNull payload: Map<String, Any>
  ): ResponseEntity<Void> {

    val task = getTask(id)
    val userIdentifier = xCurrentUserID.orElseGet { currentUserService.getCurrentUser() }
    val user = userService.getUser(userIdentifier)

    send(
      CompleteInteractionTaskCommand(
        id = task.id,
        sourceReference = task.sourceReference,
        taskDefinitionKey = task.taskDefinitionKey,
        payload = Variables.createVariables().apply { putAll(payload) },
        assignee = user.username
      )
    )

    return ResponseEntity.noContent().build()
  }


  override fun defer(
    @PathVariable("id") id: String,
    @RequestHeader(value = "X-Current-User-ID", required = false) xCurrentUserID: Optional<String>,
    @Valid @RequestBody @NotNull followUpDate: OffsetDateTime
  ): ResponseEntity<Void> {
    val task = getTask(id)

    send(
      DeferInteractionTaskCommand(
        id = task.id,
        sourceReference = task.sourceReference,
        taskDefinitionKey = task.taskDefinitionKey,
        followUpDate = Date.from(followUpDate.toInstant())
      )
    )

    return ResponseEntity.noContent().build()
  }

  override fun undefer(
    @PathVariable("id") id: String,
    @RequestHeader(value = "X-Current-User-ID", required = false) xCurrentUserID: Optional<String>
  ): ResponseEntity<Void> {
    val task = getTask(id)

    send(
      UndeferInteractionTaskCommand(
        id = task.id,
        sourceReference = task.sourceReference,
        taskDefinitionKey = task.taskDefinitionKey
      )
    )

    return ResponseEntity.noContent().build()
  }

  private fun getTask(id: String): Task = queryGateway.query(TaskForIdQuery(id), Task::class.java).join()
    ?: throw ElementNotFoundException()


  private fun send(command: InteractionTaskCommand) {
    gateway.send<Any, Any?>(command) { m, r -> logger.debug("Successfully submitted command $m, $r") }
  }
}


