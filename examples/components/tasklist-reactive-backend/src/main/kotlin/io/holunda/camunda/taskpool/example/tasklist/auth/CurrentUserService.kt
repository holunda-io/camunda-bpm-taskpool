package io.holunda.camunda.taskpool.example.tasklist.auth

/**
 * Retrieves an id of current user. This id is then used as a parameter
 * of the {@see UserService} to retrieve the user object.
 */
interface CurrentUserService {
  /**
   * Retrieves the id of of the current user.
   */
  fun getCurrentUser() : String
}
