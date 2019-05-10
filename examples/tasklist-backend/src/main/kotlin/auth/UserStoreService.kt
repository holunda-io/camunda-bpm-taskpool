package io.holunda.camunda.taskpool.example.tasklist.auth

import io.holunda.camunda.taskpool.view.auth.User

/**
 * Simple abstraction of the user store.
 * <p>
 *     this interface and its implementation is required for the show case only and should not be used in production code.
 * </p>
 */
interface UserStoreService {

  /**
   * Retrieves the list of all users in the system.
   */
  fun getUsers() : List<User>

  /**
   * Reitrieves the list of all users' identifiers.
   */
  fun getUserIdentifiers(): List<String>
}
