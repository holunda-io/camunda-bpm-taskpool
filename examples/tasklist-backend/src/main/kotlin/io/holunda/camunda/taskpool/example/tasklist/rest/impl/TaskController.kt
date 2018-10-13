package io.holunda.camunda.taskpool.example.tasklist.rest.impl

import io.holunda.camunda.taskpool.example.tasklist.auth.CurrentUserService
import io.holunda.camunda.taskpool.example.tasklist.rest.Rest
import io.holunda.camunda.taskpool.example.tasklist.rest.api.TasksApi
import io.holunda.camunda.taskpool.example.tasklist.rest.filter.filter
import io.holunda.camunda.taskpool.example.tasklist.rest.model.TaskDto
import io.holunda.camunda.taskpool.view.TasksWithDataEntries
import io.holunda.camunda.taskpool.view.auth.UserService
import io.holunda.camunda.taskpool.view.query.TasksDataEntryForUserQuery
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiParam
import mu.KLogging
import org.axonframework.queryhandling.QueryGateway
import org.axonframework.queryhandling.responsetypes.ResponseTypes
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.ok
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.*


@RestController
@RequestMapping(Rest.REQUEST_PATH)
open class TaskController(
  private val currentUserService: CurrentUserService,
  private val userService: UserService,
  private val queryGateway: QueryGateway
) : TasksApi {

  companion object : KLogging()

  override fun getTasks(
    @RequestParam(value = "page") page: Optional<String>,
    @RequestParam(value = "size") size: Optional<String>,
    @RequestParam(value = "sort") sort: Optional<String>,
    @RequestParam(value = "filter") filters: Optional<List<String>>
  ): ResponseEntity<List<TaskDto>> {

    logger.info("page=$page, size=$size, sort=$sort, filter=$filters")

    val username = currentUserService.getCurrentUser()
    val user = userService.getUser(username)


    // val allTasks = queryGateway.query(TasksForUserQuery(user), ResponseTypes.multipleInstancesOf(Task::class.java))
    val all: List<TasksWithDataEntries> = queryGateway
      .query(TasksDataEntryForUserQuery(user), ResponseTypes.multipleInstancesOf(TasksWithDataEntries::class.java))
      .join()

    val filtered = if (filters.isPresent) {
      filter(filters.get(), all)
    } else {
      all
    }


    return ok(filtered.map { TaskDto().id(it.task.id)}
    )
  }


  @ApiOperation("Loads all tasks for given user with amount greater than specified.")
  @GetMapping("/tasks-with-data")
  open fun getTasksWithDataForUser(
    @RequestParam("username") @ApiParam("Username of the user.", required = true) username: String,
    @RequestParam("amount") @ApiParam("Amount of the request.", required = true) amount: String
  ): ResponseEntity<List<TasksWithDataEntries>> {

    val user = userService.getUser(username)

    return ok(
      queryGateway
        .query(TasksDataEntryForUserQuery(user), ResponseTypes.multipleInstancesOf(TasksWithDataEntries::class.java))
        .join()
      /*
      .filter { task ->
        task.dataEntries
          .asSequence()
          .filter { it.entryType == BusinessDataEntry.REQUEST }
          .map { it.payload as Request }
          .map { it.amount }
          .reduce { acc, value -> if (value > acc) value else acc } >= BigDecimal(amount)
      } */)
  }

}
