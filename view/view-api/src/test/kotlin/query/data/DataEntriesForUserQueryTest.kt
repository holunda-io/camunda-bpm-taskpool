package io.holunda.polyflow.view.query.data

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.holunda.polyflow.bus.jackson.configurePolyflowJacksonObjectMapper
import io.holunda.polyflow.view.DataEntry
import io.holunda.polyflow.view.Task
import io.holunda.polyflow.view.auth.User
import org.assertj.core.api.Assertions.assertThat
import org.camunda.bpm.engine.variable.Variables
import org.junit.Test

internal class DataEntriesForUserQueryTest {

  private val dataEntry = DataEntry(
    entryType = "io.type",
    entryId = "0239480234",
    type = "data entry",
    applicationName = "test-application",
    name = "Data Entry for case 4711",
    payload = Variables.createVariables().apply {
      put("case", "4711")
      put("other", "SADFSA")
    },
    authorizedUsers = setOf("kermit"),
    authorizedGroups = setOf("muppets")
  )

  @Test
  fun `should test authorization`() {
    assertThat(DataEntriesForUserQuery(user = User("kermit", setOf("muppets"))).applyFilter(dataEntry)).isTrue
    assertThat(DataEntriesForUserQuery(user = User("piggy", setOf("muppets"))).applyFilter(dataEntry)).isTrue
    assertThat(DataEntriesForUserQuery(user = User("kermit", setOf("avengers"))).applyFilter(dataEntry)).isTrue
    assertThat(DataEntriesForUserQuery(user = User("ironman", setOf("avengers"))).applyFilter(dataEntry)).isFalse
    assertThat(DataEntriesForUserQuery(user = User("ironman", setOf())).applyFilter(dataEntry)).isFalse
  }

  @Test
  fun `should test filters and authorization`() {
    assertThat(DataEntriesForUserQuery(filters = listOf("case=4711"), user = User("kermit", setOf("muppets"))).applyFilter(dataEntry)).isTrue
    assertThat(DataEntriesForUserQuery(filters = listOf("data.type=data entry"), user = User("kermit", setOf("muppets"))).applyFilter(dataEntry)).isTrue
    assertThat(DataEntriesForUserQuery(filters = listOf("data.type=other type"), user = User("kermit", setOf("muppets"))).applyFilter(dataEntry)).isFalse
    assertThat(DataEntriesForUserQuery(filters = listOf("cas=4711"), user = User("kermit", setOf("muppets"))).applyFilter(dataEntry)).isFalse
    assertThat(DataEntriesForUserQuery(filters = listOf("case=4712"), user = User("kermit", setOf("muppets"))).applyFilter(dataEntry)).isFalse

    assertThat(DataEntriesForUserQuery(filters = listOf("case=4711"), user = User("piggy", setOf("muppets"))).applyFilter(dataEntry)).isTrue
    assertThat(DataEntriesForUserQuery(filters = listOf("data.type=data entry"), user = User("piggy", setOf("muppets"))).applyFilter(dataEntry)).isTrue
    assertThat(DataEntriesForUserQuery(filters = listOf("data.type=other type"), user = User("piggy", setOf("muppets"))).applyFilter(dataEntry)).isFalse
    assertThat(DataEntriesForUserQuery(filters = listOf("cas=4711"), user = User("piggy", setOf("muppets"))).applyFilter(dataEntry)).isFalse
    assertThat(DataEntriesForUserQuery(filters = listOf("case=4712"), user = User("piggy", setOf("muppets"))).applyFilter(dataEntry)).isFalse

    assertThat(DataEntriesForUserQuery(filters = listOf("case=4711"), user = User("kermit", setOf("avengers"))).applyFilter(dataEntry)).isTrue
    assertThat(DataEntriesForUserQuery(filters = listOf("data.type=data entry"), user = User("kermit", setOf("avengers"))).applyFilter(dataEntry)).isTrue
    assertThat(DataEntriesForUserQuery(filters = listOf("data.type=other type"), user = User("kermit", setOf("avengers"))).applyFilter(dataEntry)).isFalse
    assertThat(DataEntriesForUserQuery(filters = listOf("cas=4711"), user = User("kermit", setOf("avengers"))).applyFilter(dataEntry)).isFalse
    assertThat(DataEntriesForUserQuery(filters = listOf("case=4712"), user = User("kermit", setOf("avengers"))).applyFilter(dataEntry)).isFalse

    assertThat(DataEntriesForUserQuery(filters = listOf("case=4711"), user = User("ironman", setOf("avengers"))).applyFilter(dataEntry)).isFalse
    assertThat(DataEntriesForUserQuery(filters = listOf("data.type=data entry"), user = User("ironman", setOf("avengers"))).applyFilter(dataEntry)).isFalse
    assertThat(DataEntriesForUserQuery(filters = listOf("data.type=other type"), user = User("ironman", setOf("avengers"))).applyFilter(dataEntry)).isFalse
    assertThat(DataEntriesForUserQuery(filters = listOf("cas=4711"), user = User("ironman", setOf("avengers"))).applyFilter(dataEntry)).isFalse
    assertThat(DataEntriesForUserQuery(filters = listOf("case=4712"), user = User("ironman", setOf("avengers"))).applyFilter(dataEntry)).isFalse

  }


  @Test
  fun `can serialize and deserialize query with jackson`() {
    val query = DataEntriesForUserQuery(user = User("kermit", setOf("muppets")), filters = listOf("foo"))

    val om = jacksonObjectMapper().configurePolyflowJacksonObjectMapper()

    val json = om.writeValueAsString(query)

    val deserialized = om.readValue<DataEntriesForUserQuery>(json)

    assertThat(deserialized).isEqualTo(query)
  }
}
