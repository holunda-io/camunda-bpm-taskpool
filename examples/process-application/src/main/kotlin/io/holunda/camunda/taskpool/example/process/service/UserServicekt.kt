package io.holunda.camunda.taskpool.example.process.service

import io.holunda.camunda.taskpool.view.simple.service.User
import io.holunda.camunda.taskpool.view.simple.service.UserService
import org.springframework.stereotype.Component

@Component
class SimpleUserService : UserService {

  private val muppetUsers = mutableMapOf<String, User>(
    "kermit" to User("kermit", setOf("muppetshow")),
    "piggy" to User("piggy", setOf("muppetshow")),
    "gonzo" to User("gonzo", setOf("muppetshow")),
    "fozzy" to User("fozzy", setOf("muppetshow"))
  )

  override fun getUser(username: String): User {
    return muppetUsers[username] ?: throw IllegalArgumentException("Unknown user $username")
  }

}
