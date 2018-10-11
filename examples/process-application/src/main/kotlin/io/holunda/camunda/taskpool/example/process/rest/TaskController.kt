package io.holunda.camunda.taskpool.example.process.rest

import io.holunda.camunda.taskpool.example.process.service.BusinessDataEntry
import io.holunda.camunda.taskpool.example.process.service.Request
import io.holunda.camunda.taskpool.view.simple.query.TasksDataEntryForUserQuery
import io.holunda.camunda.taskpool.view.simple.query.TasksForUserQuery
import io.holunda.camunda.taskpool.view.simple.service.*
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiParam
import org.axonframework.queryhandling.QueryGateway
import org.axonframework.queryhandling.responsetypes.ResponseTypes
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.ok
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.math.BigDecimal

@Api("Task Controller")
@RestController
open class TaskController(

  private val userService: UserService,
  private val queryGateway: QueryGateway
) {

  @ApiOperation("Loads all tasks for a given user.")
  @GetMapping("/tasks")
  open fun getTasksForUser(@RequestParam("username") @ApiParam("Username of the user.", required = true) username: String): ResponseEntity<List<Task>> {

    val user = userService.getUser(username)
    return ok(queryGateway.query(TasksForUserQuery(user), ResponseTypes.multipleInstancesOf(Task::class.java)).join())
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
        .filter { task ->
          task.dataEntries
            .asSequence()
            .filter { it.entryType == BusinessDataEntry.REQUEST }
            .map { it.payload as Request }
            .map { it.amount }
            .reduce { acc, value -> if (value > acc) value else acc } >= BigDecimal(amount)
        })
  }

}
