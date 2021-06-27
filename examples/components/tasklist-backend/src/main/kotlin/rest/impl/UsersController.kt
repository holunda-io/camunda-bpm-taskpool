package io.holunda.camunda.taskpool.example.tasklist.rest.impl

import io.holunda.camunda.taskpool.example.tasklist.rest.Rest
import io.holunda.camunda.taskpool.example.tasklist.rest.api.UsersApi
import io.holunda.camunda.taskpool.example.tasklist.rest.model.UserInfoDto
import io.holunda.camunda.taskpool.example.users.UserStoreService
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.ok
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Controller to retrieve the users available in the system.
 */
@RestController
@CrossOrigin
@RequestMapping(Rest.REQUEST_PATH)
class UsersController(private val userStoreService: UserStoreService) : UsersApi {

  override fun getUsers(): ResponseEntity<List<UserInfoDto>> {
    return ok(
      userStoreService.getUserIdentifiers().map { UserInfoDto().id(it.key).username(it.value) }
    )
  }
}
