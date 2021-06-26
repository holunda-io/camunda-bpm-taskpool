package io.holunda.camunda.taskpool.example.users

import io.holunda.polyflow.view.auth.User

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
   * Retrieves the list of all users' identifiers.
   */
  fun getUserIdentifiers(): Map<String, String>
}
