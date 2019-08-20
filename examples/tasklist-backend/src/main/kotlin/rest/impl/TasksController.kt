package io.holunda.camunda.taskpool.example.tasklist.rest.impl

import io.holunda.camunda.taskpool.example.tasklist.auth.CurrentUserService
import io.holunda.camunda.taskpool.example.tasklist.rest.Rest
import io.holunda.camunda.taskpool.example.tasklist.rest.api.TasksApi
import io.holunda.camunda.taskpool.example.tasklist.rest.mapper.TaskWithDataEntriesMapper
import io.holunda.camunda.taskpool.example.tasklist.rest.model.TaskWithDataEntriesDto
import io.holunda.camunda.taskpool.view.auth.UserService
import io.holunda.camunda.taskpool.view.query.task.TasksWithDataEntriesForUserQuery
import io.holunda.camunda.taskpool.view.query.task.TasksWithDataEntriesQueryResult
import mu.KLogging
import org.axonframework.messaging.responsetypes.ResponseTypes
import org.axonframework.queryhandling.QueryGateway
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.ok
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.*


@RestController
@RequestMapping(Rest.REQUEST_PATH)
class TasksController(
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
    @RequestParam(value = "sort") sort: Optional<String>,
    @RequestHeader(value = "X-Current-User-ID", required = false) xCurrentUserID: Optional<String>
  ): ResponseEntity<List<TaskWithDataEntriesDto>> {

    val userIdentifier = xCurrentUserID.orElseGet { currentUserService.getCurrentUser() }
    val user = userService.getUser(userIdentifier)

    @Suppress("UNCHECKED_CAST")
    val result: TasksWithDataEntriesQueryResult = queryGateway
      .query(TasksWithDataEntriesForUserQuery(
        user = user,
        page = page.orElse(1),
        size = size.orElse(Int.MAX_VALUE),
        sort = sort.orElseGet { "" },
        filters = filters
      ), ResponseTypes.instanceOf(TasksWithDataEntriesQueryResult::class.java))
      .join()


    val responseHeaders = HttpHeaders().apply {
      this[HEADER_ELEMENT_COUNT] = result.totalElementCount.toString()
    }

    return ok()
      .headers(responseHeaders)
      .body(result.elements.map { mapper.dto(it) })
  }
}
