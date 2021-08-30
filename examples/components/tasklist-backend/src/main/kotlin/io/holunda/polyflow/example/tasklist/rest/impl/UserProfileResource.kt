package io.holunda.polyflow.example.tasklist.rest.impl

import io.holunda.polyflow.example.tasklist.auth.CurrentUserService
import io.holunda.polyflow.example.tasklist.rest.Rest
import io.holunda.polyflow.example.tasklist.rest.api.ProfileApi
import io.holunda.polyflow.example.tasklist.rest.model.UserProfileDto
import io.holunda.polyflow.view.auth.User
import io.holunda.polyflow.view.auth.UserService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.*

@RestController
@CrossOrigin
@RequestMapping(Rest.REQUEST_PATH)
class UserProfileResource(
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
