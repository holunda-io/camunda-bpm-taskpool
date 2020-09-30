package io.holunda.camunda.taskpool.view.simple.service

import com.tngtech.jgiven.Stage
import com.tngtech.jgiven.annotation.*
import com.tngtech.jgiven.integration.spring.JGivenStage
import io.holunda.camunda.taskpool.api.business.DataEntryCreatedEvent
import io.holunda.camunda.taskpool.api.business.DataEntryUpdatedEvent
import io.holunda.camunda.taskpool.view.DataEntry
import io.holunda.camunda.taskpool.view.auth.User
import io.holunda.camunda.taskpool.view.query.data.DataEntriesForUserQuery
import org.assertj.core.api.Assertions
import org.axonframework.messaging.MetaData
import org.axonframework.queryhandling.QueryUpdateEmitter
import org.mockito.Mockito

@JGivenStage
class DataPoolStage<SELF : DataPoolStage<SELF>> : Stage<SELF>() {

  @ScenarioState
  lateinit var testee: DataEntryService

  @BeforeScenario
  fun init() {
    testee = DataEntryService(Mockito.mock(QueryUpdateEmitter::class.java))
  }

  fun data_entry_created_event(event: DataEntryCreatedEvent): SELF {
    testee.on(event, MetaData.emptyInstance())
    return self()
  }

  fun data_entry_updated_event(event: DataEntryUpdatedEvent): SELF {
    testee.on(event, MetaData.emptyInstance())
    return self()
  }
}

@JGivenStage
open class DataPoolGivenStage<SELF : DataPoolGivenStage<SELF>> : DataPoolStage<SELF>() {

  @ProvidedScenarioState(resolution = ScenarioState.Resolution.NAME)
  private var dataEntries: List<DataEntry> = listOf()

  private fun data(i: Int, applicationName: String = "app") = TestDataEntry(entryId = "entry-$i", name = "Test entry $i")

  fun none_exists(): SELF {
    dataEntries = listOf()
    return self()
  }

  @As("$ data entries exist")
  fun entries_exist(numTasks: Int): SELF {
    dataEntries = (0 until numTasks).map { data(it) }.also { createDataInTestee(it) }.map { it.asDataEntry() }
    return self()
  }

  private fun createDataInTestee(entries: List<TestDataEntry>) {
    entries.forEach { testee.on(it.asCreatedEvent(), MetaData.emptyInstance()) }
  }
}

@JGivenStage
class DataPoolWhenStage<SELF : DataPoolWhenStage<SELF>> : DataPoolStage<SELF>() {

  @ExpectedScenarioState(resolution = ScenarioState.Resolution.NAME, required = true)
  private lateinit var dataEntries: List<DataEntry>

  @ProvidedScenarioState(resolution = ScenarioState.Resolution.NAME)
  private var queriedEntries: MutableList<DataEntry> = mutableListOf()

  private fun query(sort: String, filters: List<String>) = DataEntriesForUserQuery(User("kermit", setOf()), 1, Integer.MAX_VALUE, sort, filters)

  fun data_queried(filters: List<String>): SELF {
    queriedEntries.addAll(testee.query(query("+name", filters)).elements)
    return self()
  }
}

@JGivenStage
class DataPoolThenStage<SELF : DataPoolThenStage<SELF>> : DataPoolStage<SELF>() {

  @ExpectedScenarioState(resolution = ScenarioState.Resolution.NAME, required = true)
  lateinit var dataEntries: List<DataEntry>

  @ExpectedScenarioState(resolution = ScenarioState.Resolution.NAME, required = true)
  lateinit var queriedEntries: MutableList<DataEntry>

  @As("returns expected entries")
  fun dataEntriesAreReturned(@Hidden vararg expected: DataEntry): SELF {
    Assertions.assertThat(queriedEntries).containsExactlyInAnyOrder(*expected)
    return self()
  }

}

