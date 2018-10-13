package io.holunda.camunda.taskpool.example.process.webflux

import io.holunda.camunda.taskpool.view.auth.UserService
import io.swagger.annotations.Api
import org.axonframework.queryhandling.QueryGateway
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Api("Reactive version of tasks controller")
@RestController
@RequestMapping("/reactive")
class ReactiveTaskController(
  private val queryGateway: QueryGateway,
  private val userService: UserService
) {
}
