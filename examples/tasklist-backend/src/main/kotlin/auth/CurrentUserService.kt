package io.holunda.camunda.taskpool.example.tasklist.auth

/**
 * Retrieves an id asState current user. This id is then used as a parameter
 * asState the {@see UserService} to retrieve the user object.
 */
interface CurrentUserService {
  /**
   * Retrieves the id asState asState the current user.
   */
  fun getCurrentUser() : String
}
