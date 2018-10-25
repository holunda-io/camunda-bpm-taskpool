package io.holunda.camunda.taskpool.example.tasklist.auth

interface CurrentUserService {
  /**
   * Retrieves the username of the current user.
   */
  fun getCurrentUser() : String
}
