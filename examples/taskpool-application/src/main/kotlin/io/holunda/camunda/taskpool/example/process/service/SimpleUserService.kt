package io.holunda.camunda.taskpool.example.process.service

import io.holunda.camunda.taskpool.example.tasklist.auth.CurrentUserService
import io.holunda.camunda.taskpool.example.tasklist.auth.UserStoreService
import io.holunda.camunda.taskpool.view.auth.User
import io.holunda.camunda.taskpool.view.auth.UserService
import org.springframework.stereotype.Component

@Component
class SimpleUserService : UserService, UserStoreService {

  private val muppetUsers = mutableMapOf(
    "Ironman" to User("ironman", setOf("avengers")),
    "Hulk" to User("hulk", setOf("avengers")),
    "Piggy" to User("piggy", setOf("muppetshow")),
    "Kermit" to User("kermit", setOf("muppetshow")),
    "Gonzo" to User("gonzo", setOf("muppetshow")),
    "Fozzy" to User("fozzy", setOf("muppetshow"))
  )

  override fun getUser(userIdentifier: String): User {
    return muppetUsers[userIdentifier] ?: throw IllegalArgumentException("Unknown user $userIdentifier")
  }

  override fun getUsers(): List<User> = muppetUsers.values.toList()

  override fun getUserIdentifiers(): List<String> = muppetUsers.keys.toList()
}

