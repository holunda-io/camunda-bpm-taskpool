package io.holunda.camunda.taskpool.view.simple.service

interface UserService {
  @Throws(IllegalArgumentException::class)
  fun getUser(username: String): User
}

data class User(val username: String, val groups: Set<String>)
