package io.holunda.polyflow.view.simple.service

import com.tngtech.jgiven.Stage
import com.tngtech.jgiven.annotation.*
import com.tngtech.jgiven.integration.spring.JGivenStage
import io.holunda.camunda.taskpool.api.business.DataEntryCreatedEvent
import io.holunda.camunda.taskpool.api.business.DataEntryUpdatedEvent
import io.holunda.polyflow.view.DataEntry
import io.holunda.polyflow.view.auth.User
import io.holunda.polyflow.view.query.data.DataEntriesForUserQuery
import io.toolisticon.testing.jgiven.step
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.axonframework.messaging.MetaData
import org.axonframework.queryhandling.QueryUpdateEmitter
import org.mockito.Mockito

@JGivenStage
abstract class DataPoolStage<SELF : DataPoolStage<SELF>> : Stage<SELF>() {

  @ScenarioState
  lateinit var testee: SimpleDataEntryService

  @BeforeScenario
  fun init() {
    testee = SimpleDataEntryService(Mockito.mock(QueryUpdateEmitter::class.java))
  }

  fun data_entry_created_event(event: DataEntryCreatedEvent) = step {
    testee.on(event, MetaData.emptyInstance())
  }

  fun data_entry_updated_event(event: DataEntryUpdatedEvent) = step {
    testee.on(event, MetaData.emptyInstance())
  }
}

@JGivenStage
class DataPoolGivenStage<SELF : DataPoolGivenStage<SELF>> : DataPoolStage<SELF>() {

  @ProvidedScenarioState(resolution = ScenarioState.Resolution.NAME)
  private var dataEntries: List<DataEntry> = listOf()

  private fun data(i: Int) = TestDataEntry(entryId = "entry-$i", name = "Test entry $i")

  fun none_exists() = step {
    dataEntries = listOf()
  }

  @As("$ data entries exist")
  fun entries_exist(numTasks: Int) = step {
    dataEntries = (0 until numTasks).map { data(it) }.also { createDataInTestee(it) }.map { it.asDataEntry() }
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

  fun data_queried(filters: List<String>) = step {
    queriedEntries.addAll(testee.query(query("+name", filters)).payload.elements)
  }
}

@JGivenStage
class DataPoolThenStage<SELF : DataPoolThenStage<SELF>> : DataPoolStage<SELF>() {

  @ExpectedScenarioState(resolution = ScenarioState.Resolution.NAME, required = true)
  lateinit var dataEntries: List<DataEntry>

  @ExpectedScenarioState(resolution = ScenarioState.Resolution.NAME, required = true)
  lateinit var queriedEntries: MutableList<DataEntry>

  @As("returns expected entries")
  fun dataEntriesAreReturned(@Hidden vararg expected: DataEntry) = step {
    assertThat(queriedEntries).containsExactlyInAnyOrder(*expected)
  }

}

