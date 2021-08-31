package io.holunda.polyflow.example.tasklist.rest.impl

import io.holunda.polyflow.example.tasklist.auth.CurrentUserService
import io.holunda.polyflow.example.tasklist.rest.Rest
import io.holunda.polyflow.example.tasklist.rest.api.TasksApi
import io.holunda.polyflow.example.tasklist.rest.mapper.TaskWithDataEntriesMapper
import io.holunda.polyflow.example.tasklist.rest.model.TaskWithDataEntriesDto
import io.holunda.polyflow.view.auth.UserService
import io.holunda.polyflow.view.query.task.TasksWithDataEntriesForUserQuery
import io.holunda.polyflow.view.query.task.TasksWithDataEntriesQueryResult
import mu.KLogging
import org.axonframework.messaging.responsetypes.ResponseTypes
import org.axonframework.queryhandling.QueryGateway
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.ok
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@CrossOrigin
@RequestMapping(Rest.REQUEST_PATH)
class TasksResource(
  private val currentUserService: CurrentUserService,
  private val userService: UserService,
  private val queryGateway: QueryGateway,
  private val mapper: TaskWithDataEntriesMapper
) : TasksApi {

  companion object : KLogging() {
    const val HEADER_ELEMENT_COUNT = "X-ElementCount"
  }

  override fun getTasks(
    @RequestParam(value = "page") page: Optional<Int>,
    @RequestParam(value = "size") size: Optional<Int>,
    @RequestParam(value = "sort") sort: Optional<String>,
    @RequestParam(value = "filter") filters: Optional<List<String>>,
    @RequestHeader(value = "X-Current-User-ID", required = false) xCurrentUserID: Optional<String>
  ): ResponseEntity<List<TaskWithDataEntriesDto>> {

    val userIdentifier = xCurrentUserID.orElseGet { currentUserService.getCurrentUser() }
    val user = userService.getUser(userIdentifier)

    @Suppress("UNCHECKED_CAST")
    val result: TasksWithDataEntriesQueryResult = queryGateway
      .query(
        TasksWithDataEntriesForUserQuery(
          user = user,
          page = page.orElse(1),
          size = size.orElse(Int.MAX_VALUE),
          sort = sort.orElseGet { "" },
          filters = filters.orElseGet { listOf() }
        ), ResponseTypes.instanceOf(TasksWithDataEntriesQueryResult::class.java)
      )
      .join()


    val responseHeaders = HttpHeaders().apply {
      this[HEADER_ELEMENT_COUNT] = result.totalElementCount.toString()
    }

    return ok()
      .headers(responseHeaders)
      .body(result.elements.map { mapper.dto(it) })
  }
}
