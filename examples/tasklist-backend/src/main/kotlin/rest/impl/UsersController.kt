package io.holunda.camunda.taskpool.example.tasklist.rest.impl

import io.holunda.camunda.taskpool.example.tasklist.auth.UserStoreService
import io.holunda.camunda.taskpool.example.tasklist.rest.Rest
import io.holunda.camunda.taskpool.example.tasklist.rest.api.UsersApi
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.ok
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(Rest.REQUEST_PATH)
class UsersController(private val userStoreService: UserStoreService) : UsersApi {

  override fun getUsers(): ResponseEntity<Map<String, String>> {
    return ok(userStoreService.getUserIdentifiers())
  }
}
