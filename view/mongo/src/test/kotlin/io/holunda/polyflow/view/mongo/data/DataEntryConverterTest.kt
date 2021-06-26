package io.holunda.polyflow.view.mongo.data

import io.holunda.camunda.taskpool.api.business.AddAuthorization
import io.holunda.camunda.taskpool.api.business.DataEntryCreatedEvent
import io.holunda.polyflow.view.mongo.repository.toDocument
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.util.*

internal class DataEntryConverterTest {

  @Test
  fun `convert users and groups to document`() {
    val dataEntryCreated = DataEntryCreatedEvent(
      entryId = UUID.randomUUID().toString(),
      entryType = "io.polyflow.TestType",
      type = "Test Entry",
      applicationName = "app1",
      name = "Test Data Entry 4711",
      authorizations = listOf(
        AddAuthorization(listOf("kermit", "piggy"), listOf("the avengers", "muppet show"))
      )
    )

    val mongoDocument = dataEntryCreated.toDocument()

    assertThat(mongoDocument.authorizedPrincipals).containsExactlyInAnyOrder("user:kermit", "user:piggy", "group:the avengers", "group:muppet show")
    assertThat(mongoDocument.getAuthorizedGroups()).containsExactlyInAnyOrder("the avengers", "muppet show")
    assertThat(mongoDocument.getAuthorizedUsers()).containsExactlyInAnyOrder("kermit", "piggy")

  }
}
