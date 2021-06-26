package io.holunda.polyflow.view.auth

/**
 * Simple user abstraction.
 * @param username username of the user.
 * @param groups set of group names assigned to the user.
 */
data class User(
  val username: String,
  val groups: Set<String>
)
