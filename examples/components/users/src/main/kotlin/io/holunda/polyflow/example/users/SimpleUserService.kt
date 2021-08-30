package io.holunda.polyflow.example.users

import io.holunda.polyflow.view.auth.UnknownUserException
import io.holunda.polyflow.view.auth.User
import io.holunda.polyflow.view.auth.UserService
import org.springframework.stereotype.Component

@Component
class SimpleUserService : UserService, UserStoreService {

  private val muppetUsers = mutableMapOf(
    "5939c52b-d560-4c72-b6e5-61b7996361c4" to User("ironman", setOf("avengers")),
    "664313ad-58e0-46d8-a186-e9654bad845f" to User("hulk", setOf("avengers")),
    "98496787-eb18-4fde-9075-a82194132efd" to User("piggy", setOf("muppetshow")),
    "37bff195-06fe-4788-9480-a7d9ac6474e1" to User("kermit", setOf("muppetshow")),
    "27b8aa55-42a2-407a-be9d-d956188aa334" to User("gonzo", setOf("muppetshow")),
    "394ffece-9412-4ba6-8b3e-57236c356881" to User("fozzy", setOf("muppetshow"))
  )

  override fun getUser(userIdentifier: String): User {
    return muppetUsers[userIdentifier] ?: throw UnknownUserException("Unknown user identified by '$userIdentifier'")
  }

  override fun getUsers(): List<User> = muppetUsers.values.toList()

  override fun getUserIdentifiers(): Map<String, String> = muppetUsers.map { (key, value) -> key to value.username }.toMap()
}
