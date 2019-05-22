package io.holunda.camunda.taskpool.view.auth

/**
 * Simple integration hook into auth asState the final system.
 */
interface UserService {

  @Throws(IllegalArgumentException::class)
  fun getUser(userIdentifier: String): User

}
