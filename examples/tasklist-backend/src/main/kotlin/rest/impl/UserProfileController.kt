package io.holunda.camunda.taskpool.example.tasklist.rest.impl

import io.holunda.camunda.taskpool.example.tasklist.auth.CurrentUserService
import io.holunda.camunda.taskpool.example.tasklist.rest.Rest
import io.holunda.camunda.taskpool.example.tasklist.rest.api.ProfileApi
import io.holunda.camunda.taskpool.example.tasklist.rest.model.UserProfileDto
import io.holunda.camunda.taskpool.view.auth.User
import io.holunda.camunda.taskpool.view.auth.UserService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(Rest.REQUEST_PATH)
class UserProfileController(
  private val currentUserService: CurrentUserService,
  private val userService: UserService
) : ProfileApi {
  override fun getProfile(): ResponseEntity<UserProfileDto> {
    // retrieve the user
    val user: User = userService.getUser(currentUserService.getCurrentUser())

    return ResponseEntity.ok(UserProfileDto().username(user.username))
  }
}
