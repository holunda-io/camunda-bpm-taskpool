package io.holunda.camunda.taskpool.example.tasklist.rest.impl

import io.holunda.camunda.taskpool.api.task.ClaimInteractionTaskCommand
import io.holunda.camunda.taskpool.api.task.CompleteInteractionTaskCommand
import io.holunda.camunda.taskpool.api.task.InteractionTaskCommand
import io.holunda.camunda.taskpool.api.task.UnclaimInteractionTaskCommand
import io.holunda.camunda.taskpool.example.tasklist.auth.CurrentUserService
import io.holunda.camunda.taskpool.example.tasklist.rest.ElementNotFoundException
import io.holunda.camunda.taskpool.example.tasklist.rest.Rest
import io.holunda.camunda.taskpool.example.tasklist.rest.api.TaskApi
import io.holunda.camunda.taskpool.example.tasklist.rest.model.PayloadDto
import io.holunda.camunda.taskpool.view.Task
import io.holunda.camunda.taskpool.view.auth.UserService
import io.holunda.camunda.taskpool.view.query.TaskForIdQuery
import mu.KLogging
import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.queryhandling.QueryGateway
import org.camunda.bpm.engine.variable.Variables
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.validation.Valid

@RestController
@RequestMapping(Rest.REQUEST_PATH)
class TaskController(
  private val gateway: CommandGateway,
  private val queryGateway: QueryGateway,
  private val currentUserService: CurrentUserService,
  private val userService: UserService
) : TaskApi {

  companion object : KLogging()

  override fun claim(@PathVariable("id") id: String): ResponseEntity<Void> {

    val task = getTask(id)
    val userIdentifier = currentUserService.getCurrentUser()
    val user = userService.getUser(userIdentifier)

    send(ClaimInteractionTaskCommand(
      id = task.id,
      sourceReference = task.sourceReference,
      taskDefinitionKey = task.taskDefinitionKey,
      assignee = user.username
    ))

    return ResponseEntity.noContent().build()
  }

  override fun unclaim(@PathVariable("id") id: String): ResponseEntity<Void> {

    val task = getTask(id)

    send(UnclaimInteractionTaskCommand(
      id = task.id,
      sourceReference = task.sourceReference,
      taskDefinitionKey = task.taskDefinitionKey
    ))

    return ResponseEntity.noContent().build()
  }

  override fun complete(@PathVariable("id") id: String, @Valid @RequestBody payload: PayloadDto): ResponseEntity<Void> {

    val task = getTask(id)
    val userIdentifier = currentUserService.getCurrentUser()
    val user = userService.getUser(userIdentifier)

    send(CompleteInteractionTaskCommand(
      id = task.id,
      sourceReference = task.sourceReference,
      taskDefinitionKey = task.taskDefinitionKey,
      payload = Variables.createVariables().apply { putAll(payload) },
      assignee = user.username
    ))

    return super.complete(id, payload)
  }

  internal fun getTask(id: String): Task = queryGateway.query(TaskForIdQuery(id), Task::class.java).join()
    ?: throw ElementNotFoundException()


  internal fun send(command: InteractionTaskCommand) {
    gateway.send<Any, Any?>(command) { m, r -> logger.info("Successfully submitted command $m, $r") }
  }
}


