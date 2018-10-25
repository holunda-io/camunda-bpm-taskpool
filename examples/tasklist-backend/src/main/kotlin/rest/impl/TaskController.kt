package io.holunda.camunda.taskpool.example.tasklist.rest.impl

import io.holunda.camunda.taskpool.example.tasklist.auth.CurrentUserService
import io.holunda.camunda.taskpool.example.tasklist.rest.Rest
import io.holunda.camunda.taskpool.example.tasklist.rest.api.TasksApi
import io.holunda.camunda.taskpool.example.tasklist.rest.mapper.TaskWithDataEntriesMapper
import io.holunda.camunda.taskpool.example.tasklist.rest.model.TaskWithDataEntriesDto
import io.holunda.camunda.taskpool.view.TaskWithDataEntries
import io.holunda.camunda.taskpool.view.auth.UserService
import io.holunda.camunda.taskpool.view.query.TasksDataEntryForUserQuery
import mu.KLogging
import org.axonframework.messaging.responsetypes.ResponseTypes
import org.axonframework.queryhandling.QueryGateway
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.ok
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.*


@RestController
@RequestMapping(Rest.REQUEST_PATH)
open class TaskController(
  private val currentUserService: CurrentUserService,
  private val userService: UserService,
  private val queryGateway: QueryGateway,
  private val mapper: TaskWithDataEntriesMapper
) : TasksApi {

  companion object : KLogging()

  override fun getTasks(
    @RequestParam(value = "filter") filters: List<String>,
    @RequestParam(value = "page") page: Optional<Int>,
    @RequestParam(value = "size") size: Optional<Int>,
    @RequestParam(value = "sort") sort: Optional<List<String>>
  ): ResponseEntity<List<TaskWithDataEntriesDto>> {

    val username = currentUserService.getCurrentUser()
    val user = userService.getUser(username)

    val result: List<TaskWithDataEntries> = queryGateway
      .query(TasksDataEntryForUserQuery(
        user = user,
        page = page,
        size = size,
        sort = sort.orElseGet { listOf() },
        filters = filters
      ), ResponseTypes.multipleInstancesOf(TaskWithDataEntries::class.java))
      .join()
    return ok(result.map { mapper.dto(it) })
  }
}
