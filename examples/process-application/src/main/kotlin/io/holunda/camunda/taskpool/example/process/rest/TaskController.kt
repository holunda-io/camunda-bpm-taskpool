package io.holunda.camunda.taskpool.example.process.rest

import io.holunda.camunda.taskpool.view.simple.service.Task
import io.holunda.camunda.taskpool.view.simple.service.TasksForUserQuery
import io.holunda.camunda.taskpool.view.simple.service.UserService
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiParam
import org.axonframework.queryhandling.QueryGateway
import org.axonframework.queryhandling.responsetypes.ResponseTypes
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.ok
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

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
}
