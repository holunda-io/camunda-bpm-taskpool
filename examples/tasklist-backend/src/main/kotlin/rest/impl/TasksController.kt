package io.holunda.camunda.taskpool.example.tasklist.rest.impl

import io.holunda.camunda.taskpool.example.tasklist.auth.CurrentUserService
import io.holunda.camunda.taskpool.example.tasklist.rest.Rest
import io.holunda.camunda.taskpool.example.tasklist.rest.api.TasksApi
import io.holunda.camunda.taskpool.example.tasklist.rest.mapper.TaskWithDataEntriesMapper
import io.holunda.camunda.taskpool.example.tasklist.rest.model.TaskWithDataEntriesDto
import io.holunda.camunda.taskpool.view.auth.UserService
import io.holunda.camunda.taskpool.view.query.TasksWithDataEntriesForUserQuery
import io.holunda.camunda.taskpool.view.query.TasksWithDataEntriesResponse
import mu.KLogging
import org.axonframework.messaging.responsetypes.ResponseTypes
import org.axonframework.queryhandling.QueryGateway
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.ok
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.*


@RestController
@RequestMapping(Rest.REQUEST_PATH)
open class TasksController(
  private val currentUserService: CurrentUserService,
  private val userService: UserService,
  private val queryGateway: QueryGateway,
  private val mapper: TaskWithDataEntriesMapper
) : TasksApi {

  companion object : KLogging() {
    const val HEADER_ELEMENT_COUNT = "X-ElementCount"
  }

  override fun getTasks(
    @RequestParam(value = "filter") filters: List<String>,
    @RequestParam(value = "page") page: Optional<Int>,
    @RequestParam(value = "size") size: Optional<Int>,
    @RequestParam(value = "sort") sort: Optional<String>
  ): ResponseEntity<List<TaskWithDataEntriesDto>> {

    val username = currentUserService.getCurrentUser()
    val user = userService.getUser(username)

    val result: TasksWithDataEntriesResponse = queryGateway
      .query(TasksWithDataEntriesForUserQuery(
        user = user,
        page = page.orElse(0),
        size = size.orElse(Int.MAX_VALUE),
        sort = sort.orElseGet { "" },
        filters = filters
      ), ResponseTypes.instanceOf(TasksWithDataEntriesResponse::class.java))
      .join()

    val responseHeaders = HttpHeaders().apply {
      this[HEADER_ELEMENT_COUNT] = result.elementCount.toString()
    }

    return ok()
      .headers(responseHeaders)
      .body(result.tasksWithDataEntries.map { mapper.dto(it) })
  }
}
