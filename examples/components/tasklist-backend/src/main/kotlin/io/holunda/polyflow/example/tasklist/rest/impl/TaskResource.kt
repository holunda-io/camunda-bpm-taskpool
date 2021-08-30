package io.holunda.polyflow.example.tasklist.rest.impl

import io.holunda.camunda.taskpool.api.task.*
import io.holunda.polyflow.example.tasklist.auth.CurrentUserService
import io.holunda.polyflow.example.tasklist.rest.api.TaskApi
import io.holunda.polyflow.example.tasklist.rest.ElementNotFoundException
import io.holunda.polyflow.example.tasklist.rest.Rest
import io.holunda.polyflow.view.Task
import io.holunda.polyflow.view.auth.User
import io.holunda.polyflow.view.auth.UserService
import io.holunda.polyflow.view.query.task.TaskForIdQuery
import mu.KLogging
import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.queryhandling.QueryGateway
import org.camunda.bpm.engine.variable.Variables
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.*
import java.time.OffsetDateTime
import java.util.*

@RestController
@CrossOrigin
@RequestMapping(Rest.REQUEST_PATH)
class TaskResource(
  private val taskServiceGatewayGateway: TaskServiceGateway,
  private val currentUserService: CurrentUserService,
  private val userService: UserService
) : TaskApi {

  companion object : KLogging()

  override fun claim(
    id: String,
    xCurrentUserID: Optional<String>
  ): ResponseEntity<Void> {

    val user = userService.getUser(xCurrentUserID.orElseGet { currentUserService.getCurrentUser() })
    val task = getAuthorizedTask(id, user)

    taskServiceGatewayGateway.send(
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
    id: String,
    xCurrentUserID: Optional<String>
  ): ResponseEntity<Void> {

    val user = userService.getUser(xCurrentUserID.orElseGet { currentUserService.getCurrentUser() })
    val task = getAuthorizedTask(id, user)

    taskServiceGatewayGateway.send(
      UnclaimInteractionTaskCommand(
        id = task.id,
        sourceReference = task.sourceReference,
        taskDefinitionKey = task.taskDefinitionKey
      )
    )

    return ResponseEntity.noContent().build()
  }

  override fun complete(
    id: String,
    xCurrentUserID: Optional<String>,
    payload: Map<String, Any>
  ): ResponseEntity<Void> {

    val user = userService.getUser(xCurrentUserID.orElseGet { currentUserService.getCurrentUser() })
    val task = getAuthorizedTask(id, user)

    taskServiceGatewayGateway.send(
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
    id: String,
    xCurrentUserID: Optional<String>,
    followUpDate: OffsetDateTime
  ): ResponseEntity<Void> {

    val user = userService.getUser(xCurrentUserID.orElseGet { currentUserService.getCurrentUser() })
    val task = getAuthorizedTask(id, user)

    taskServiceGatewayGateway.send(
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
    id: String,
    xCurrentUserID: Optional<String>
  ): ResponseEntity<Void> {

    val user = userService.getUser(xCurrentUserID.orElseGet { currentUserService.getCurrentUser() })
    val task = getAuthorizedTask(id, user)

    taskServiceGatewayGateway.send(
      UndeferInteractionTaskCommand(
        id = task.id,
        sourceReference = task.sourceReference,
        taskDefinitionKey = task.taskDefinitionKey
      )
    )

    return ResponseEntity.noContent().build()
  }

  private fun getAuthorizedTask(id: String, user: User): Task = taskServiceGatewayGateway.getTask(id)
    .apply {
      if (!isAuthorized(this, user)) {
        // if the user is not allowed to access, behave if the task is not found
        throw ElementNotFoundException()
      }
    }


  private fun isAuthorized(task: Task, user: User) =
    task.assignee == user.username || task.candidateUsers.contains(user.username) || task.candidateGroups.any { requiredGroup ->
      user.groups.contains(
        requiredGroup
      )
    }
}

@Component
class TaskServiceGateway(
  val queryGateway: QueryGateway,
  val commandGateway: CommandGateway
) {

  fun send(command: InteractionTaskCommand) {
    commandGateway.send<Any, Any?>(command) { m, r -> TaskResource.logger.debug("Successfully submitted command $m, $r") }
  }

  fun getTask(id: String): Task = queryGateway.query(TaskForIdQuery(id), Task::class.java).join() ?: throw ElementNotFoundException()
}

