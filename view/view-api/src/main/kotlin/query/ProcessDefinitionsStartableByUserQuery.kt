package io.holunda.camunda.taskpool.view.query

import io.holunda.camunda.taskpool.view.ProcessDefinition
import io.holunda.camunda.taskpool.view.auth.User

data class ProcessDefinitionsStartableByUserQuery(
  val user: User
) : FilterQuery<ProcessDefinition> {

  override fun applyFilter(element: ProcessDefinition) =
  // startble
    element.startableFromTasklist &&
      // candidate user
      (element.candidateStarterUsers.contains(this.user.username))
      // candidate groups:
      || (element.candidateStarterGroups.any { candidateGroup -> this.user.groups.contains(candidateGroup) })
}
