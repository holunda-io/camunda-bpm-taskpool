package io.holunda.camunda.taskpool.example.tasklist.rest.impl

import io.holunda.camunda.taskpool.example.tasklist.auth.CurrentUserService
import io.holunda.camunda.taskpool.example.tasklist.rest.Rest
import io.holunda.camunda.taskpool.example.tasklist.rest.api.ProfileApi
import io.holunda.camunda.taskpool.example.tasklist.rest.model.UserProfileDto
import io.holunda.polyflow.view.auth.User
import io.holunda.polyflow.view.auth.UserService
import io.swagger.annotations.Api
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.*

@Api(tags = ["Profile"])
@RestController
@RequestMapping(Rest.REQUEST_PATH)
class UserProfileController(
  private val currentUserService: CurrentUserService,
  private val userService: UserService
) : ProfileApi {
  override fun getProfile(@RequestHeader(value = "X-Current-User-ID", required = false) xCurrentUserID: Optional<String>): ResponseEntity<UserProfileDto> {

    val userIdentifier = xCurrentUserID.orElseGet { currentUserService.getCurrentUser() }
    // retrieve the user
    val user: User = userService.getUser(userIdentifier)

    return ResponseEntity.ok(UserProfileDto().username(user.username))
  }

}
