package io.holunda.polyflow.view.query.process

import io.holunda.polyflow.view.ProcessDefinition
import io.holunda.polyflow.view.auth.User
import io.holunda.polyflow.view.query.FilterQuery

/**
 * Query for startable processes by given user.
 * @param user user with groups accessing the startable processes.
 */
data class ProcessDefinitionsStartableByUserQuery(
  val user: User
) : FilterQuery<ProcessDefinition> {

  override fun applyFilter(element: ProcessDefinition) =
    // start-able
    element.startableFromTasklist &&
      // candidate user
      (element.candidateStarterUsers.contains(this.user.username)
      // candidate groups
      || (element.candidateStarterGroups.any { candidateGroup -> this.user.groups.contains(candidateGroup) }))
}
