package io.holunda.camunda.taskpool.view.auth

interface UserService {

  @Throws(IllegalArgumentException::class)
  fun getUser(userIdentifier: String): User
}
