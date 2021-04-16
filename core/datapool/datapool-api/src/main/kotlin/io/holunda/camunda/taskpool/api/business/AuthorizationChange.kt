package io.holunda.camunda.taskpool.api.business

/**
 * Change of authorization on the business object.
 */
sealed class AuthorizationChange {

  companion object {
    /**
     * Adds a user.
     */
    @JvmStatic
    fun addUser(username: String): AuthorizationChange = AddAuthorization(authorizedUsers = listOf(username))

    /**
     * Removes a user.
     */
    @JvmStatic
    fun removeUser(username: String): AuthorizationChange = RemoveAuthorization(authorizedUsers = listOf(username))

    /**
     * Adds a group.
     */
    @JvmStatic
    fun addGroup(groupName: String): AuthorizationChange = AddAuthorization(authorizedGroups = listOf(groupName))

    /**
     * Removes a group.
     */
    @JvmStatic
    fun removeGroup(groupName: String): AuthorizationChange = RemoveAuthorization(authorizedGroups = listOf(groupName))

    /**
     * Applies authorizations to users.
     * @return a list of authorized users.
     */
    @JvmStatic
    fun applyUserAuthorization(authorizedUsers: List<String>, authorizationChanges: List<AuthorizationChange>): List<String> {
      val usersToRemove = authorizationChanges.filterIsInstance<RemoveAuthorization>().flatMap { it.authorizedUsers }
      val usersToAdd = authorizationChanges.filterIsInstance<AddAuthorization>().flatMap { it.authorizedUsers }
      val mutable = authorizedUsers.toMutableList()
      mutable.addAll(usersToAdd)
      mutable.removeAll(usersToRemove)

      return mutable.toSet().toList()
    }

    /**
     * Applies authorizations to groups.
     * @return a list of authorized groups.
     */
    @JvmStatic
    fun applyGroupAuthorization(authorizedGroups: List<String>, authorizationChanges: List<AuthorizationChange>): List<String> {
      val groupsToRemove = authorizationChanges.filterIsInstance<RemoveAuthorization>().flatMap { it.authorizedGroups }
      val groupsToAdd = authorizationChanges.filterIsInstance<AddAuthorization>().flatMap { it.authorizedGroups }
      val mutable = authorizedGroups.toMutableList()
      mutable.addAll(groupsToAdd)
      mutable.removeAll(groupsToRemove)

      return mutable.toSet().toList()
    }

  }
}

/**
 * Grants access to data entry.
 */
data class AddAuthorization(
  /**
   * List of authorized users to grant access.
   */
  val authorizedUsers: List<String> = listOf(),
  /**
   * List of authorized groups to grant access.
   */
  val authorizedGroups: List<String> = listOf()
) : AuthorizationChange()

/**
 * Removes access to data entry.
 */
data class RemoveAuthorization(
  /**
   * List of authorized users to grant access.
   */
  val authorizedUsers: List<String> = listOf(),
  /**
   * List of authorized groups to grant access.
   */
  val authorizedGroups: List<String> = listOf()
) : AuthorizationChange()
