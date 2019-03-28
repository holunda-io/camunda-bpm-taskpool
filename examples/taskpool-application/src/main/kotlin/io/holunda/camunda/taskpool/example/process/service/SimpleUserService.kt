package io.holunda.camunda.taskpool.example.process.service

import io.holunda.camunda.taskpool.example.tasklist.auth.CurrentUserService
import io.holunda.camunda.taskpool.view.auth.User
import io.holunda.camunda.taskpool.view.auth.UserService
import org.springframework.stereotype.Component

@Component
class SimpleUserService : UserService, CurrentUserService {

  private val muppetUsers = mutableMapOf(
    "kermit" to User("kermit", setOf("muppetshow")),
    "piggy" to User("piggy", setOf("muppetshow")),
    "gonzo" to User("gonzo", setOf("muppetshow")),
    "fozzy" to User("fozzy", setOf("muppetshow"))
  )

  override fun getUser(userIdentifier: String): User {
    return muppetUsers[userIdentifier] ?: throw IllegalArgumentException("Unknown user $userIdentifier")
  }

  override fun getCurrentUser(): String = "kermit"

}
