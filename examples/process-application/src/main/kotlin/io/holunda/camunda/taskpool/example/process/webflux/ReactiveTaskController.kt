package io.holunda.camunda.taskpool.example.process.webflux

import io.holunda.camunda.taskpool.view.simple.query.TasksForUserQuery
import io.holunda.camunda.taskpool.view.simple.service.UserService
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiParam
import org.axonframework.queryhandling.QueryGateway
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.body
import reactor.core.publisher.Mono

@Api("Reactive version of tasks controller")
@RestController
@RequestMapping("/reactive")
class ReactiveTaskController(
  private val queryGateway: QueryGateway,
  private val userService: UserService
) {

  @ApiOperation("Provides a list of tasks for current user.")
  @GetMapping("/tasks")
  fun getTasksForUser(@ApiParam("Username of the user.") @RequestParam("username", required = true) username: String): Mono<ServerResponse> {

    return ServerResponse.ok().body(TasksForUserQuery(userService.getUser(username)).subscribeTo(queryGateway))
  }
}
