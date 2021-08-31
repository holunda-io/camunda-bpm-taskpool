package io.holunda.polyflow.view.query.process

import io.holunda.polyflow.view.ProcessDefinition
import io.holunda.polyflow.view.auth.User
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

internal class ProcessDefinitionsStartableByUserQueryTest {

  private val definition = ProcessDefinition(
    processDefinitionId = "def-id",
    processDefinitionKey = "def-key",
    processDefinitionVersion = 1,
    applicationName = "test-application",
    processName = "My process",
    startableFromTasklist = true,
    candidateStarterUsers = setOf("kermit"),
    candidateStarterGroups = setOf("muppets")
  )

  private val notStartable = ProcessDefinition(
    processDefinitionId = "def-id",
    processDefinitionKey = "def-key",
    processDefinitionVersion = 1,
    applicationName = "test-application",
    processName = "My process",
    startableFromTasklist = false,
    candidateStarterUsers = setOf("kermit"),
    candidateStarterGroups = setOf("muppets")
  )


  @Test
  fun `should filter by user`() {
    assertThat(ProcessDefinitionsStartableByUserQuery(user = User("kermit", setOf("muppets"))).applyFilter(definition)).isTrue
    assertThat(ProcessDefinitionsStartableByUserQuery(user = User("piggy", setOf("muppets"))).applyFilter(definition)).isTrue
    assertThat(ProcessDefinitionsStartableByUserQuery(user = User("kermit", setOf("avengers"))).applyFilter(definition)).isTrue
    assertThat(ProcessDefinitionsStartableByUserQuery(user = User("ironman", setOf("avengers"))).applyFilter(definition)).isFalse

    assertThat(ProcessDefinitionsStartableByUserQuery(user = User("kermit", setOf("muppets"))).applyFilter(notStartable)).isFalse
  }
}
