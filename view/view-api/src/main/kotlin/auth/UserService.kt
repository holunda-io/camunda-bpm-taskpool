package io.holunda.camunda.taskpool.view.auth

/**
 * Simple integration hook into auth of the final system.
 */
interface UserService {

  /**
   * Retrieves a user for given user identifier.
   * [userIdentifier] a token or a key identifying the user.
   * @return User
   * @throws IllegalArgumentException if user not found.
   */
  @Throws(IllegalArgumentException::class)
  fun getUser(userIdentifier: String): User

}
