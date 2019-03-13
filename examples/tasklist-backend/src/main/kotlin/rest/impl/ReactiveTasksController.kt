package io.holunda.camunda.taskpool.example.tasklist.rest.impl

import io.holunda.camunda.taskpool.example.tasklist.auth.CurrentUserService
import io.holunda.camunda.taskpool.example.tasklist.rest.Rest
import io.holunda.camunda.taskpool.example.tasklist.rest.mapper.TaskWithDataEntriesMapper
import io.holunda.camunda.taskpool.example.tasklist.rest.model.TaskWithDataEntriesDto
import io.holunda.camunda.taskpool.view.auth.UserService
import io.holunda.camunda.taskpool.view.query.TasksWithDataEntriesForUserQuery
import io.holunda.camunda.taskpool.view.query.TasksWithDataEntriesResponse
import mu.KLogging
import org.axonframework.messaging.responsetypes.ResponseTypes
import org.axonframework.queryhandling.QueryGateway
import org.axonframework.queryhandling.SubscriptionQueryResult
import org.springframework.http.MediaType
import org.springframework.http.server.reactive.ServerHttpResponse
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import java.util.*


/**
 * Reactive controller delivering tasks.
 */
@RestController
@RequestMapping(Rest.REACTIVE_PATH)
open class ReactiveTasksController(
  private val currentUserService: CurrentUserService,
  private val userService: UserService,
  private val queryGateway: QueryGateway,
  private val mapper: TaskWithDataEntriesMapper
) {

  companion object : KLogging()

  @GetMapping(path = ["/tasks"], produces = [MediaType.TEXT_EVENT_STREAM_VALUE, MediaType.APPLICATION_JSON_VALUE])
  fun getTasks(
    @RequestParam(value = "filter") filters: List<String>,
    @RequestParam(value = "page") page: Optional<Int>,
    @RequestParam(value = "size") size: Optional<Int>,
    @RequestParam(value = "sort") sort: Optional<String>,
    response: ServerHttpResponse
  ): Flux<TaskWithDataEntriesDto> {

    val username = currentUserService.getCurrentUser()
    val user = userService.getUser(username)

    val taskEvents: SubscriptionQueryResult<TasksWithDataEntriesResponse, TasksWithDataEntriesResponse> = queryGateway
      .subscriptionQuery(
        TasksWithDataEntriesForUserQuery(
          user = user,
          page = page.orElse(0),
          size = size.orElse(Int.MAX_VALUE),
          sort = sort.orElseGet { "" },
          filters = filters),
        ResponseTypes.instanceOf(TasksWithDataEntriesResponse::class.java),
        ResponseTypes.instanceOf(TasksWithDataEntriesResponse::class.java)
      )

    return taskEvents
      .initialResult().flatMapMany { Flux.fromIterable(it.tasksWithDataEntries) }
      .concatWith(taskEvents.updates().flatMap { Flux.fromIterable(it.tasksWithDataEntries) })
      .map {
        mapper.dto(it)
      }
  }
}
