package io.holunda.polyflow.view

import io.holunda.camunda.taskpool.api.business.CorrelationMap
import io.holunda.camunda.taskpool.api.business.addCorrelation
import io.holunda.camunda.taskpool.api.business.dataIdentityString
import io.holunda.camunda.taskpool.api.business.newCorrelations
import io.holunda.camunda.taskpool.api.task.ProcessReference
import org.assertj.core.api.Assertions.assertThat
import org.camunda.bpm.engine.variable.Variables
import org.junit.Test
import java.util.*

class TaskWithDataEntriesTest {

  private val dataEntry1 = DataEntry(
    entryType = "EntryType1",
    entryId = UUID.randomUUID().toString(),
    payload = Variables.putValue("foo", "bar"),
    type = "myType",
    applicationName = "name",
    name = "myName"
  )

  private val dataEntry2 = DataEntry(
    entryType = "EntryType2",
    entryId = UUID.randomUUID().toString(),
    payload = Variables.putValue("zee", "other"),
    type = "myType",
    applicationName = "name",
    name = "myName"
  )

  private val dataEntryList = listOf(dataEntry1, dataEntry2)
  private val dataEntryMap = dataEntryList.associateBy { it.identity }

  @Test
  fun `should correlate data entries`() {

    val correlationMap = newCorrelations()
    val dataEntries: MutableMap<String, DataEntry> = mutableMapOf()

    // add all to the data entries and provide correlations
    dataEntryList.forEach {
      dataEntries[dataIdentityString(entryType = it.entryType, entryId = it.entryId)] = it
      correlationMap.addCorrelation(entryType = it.entryType, entryId = it.entryId)
    }

    val task = createTask(correlations = correlationMap)

    val taskWithDataEntries = TaskWithDataEntries.correlate(task, dataEntries)

    assertThat(taskWithDataEntries.dataEntries).containsExactlyElementsOf(dataEntryList)
  }

  @Test
  fun `correlate task with dataEntries`() {
    val task = createTask(correlations = correlationMap(dataEntry1))

    val taskWithDataEntries = TaskWithDataEntries.correlate(task, dataEntryMap)

    assertThat(taskWithDataEntries.task).isEqualTo(task)
    assertThat(taskWithDataEntries.dataEntries).containsOnly(dataEntry1)
  }

  @Test
  fun `correlate dataEntry with tasks`() {
    val task1 = createTask(id = "1", correlations = correlationMap(dataEntry1))
    val task2 = createTask(id = "2", correlations = correlationMap(dataEntry2))

    val list = TaskWithDataEntries.correlate(listOf(task1, task2), dataEntry1)

    assertThat(list).hasSize(1)
    assertThat(list[0].task).isEqualTo(task1)
    assertThat(list[0].dataEntries).containsOnly(dataEntry1)
  }

  @Test
  fun `correlate multiple tasks with dataEntries`() {
    val task1 = createTask(id = "1", correlations = correlationMap(dataEntry1))
    val task2 = createTask(id = "2", correlations = correlationMap(dataEntry2))

    val list = TaskWithDataEntries.correlate(listOf(task1, task2), dataEntryMap)

    assertThat(list).hasSize(2)

    assertThat(list[0]).isEqualTo(TaskWithDataEntries(task1, listOf(dataEntry1)))
    assertThat(list[1]).isEqualTo(TaskWithDataEntries(task2, listOf(dataEntry2)))
  }

  private fun correlationMap(vararg dataEntries: DataEntry): CorrelationMap {
    val correlations = newCorrelations()

    dataEntries.forEach { correlations.addCorrelation(it.entryType, it.entryId) }

    return correlations
  }

  private fun createTask(id: String = UUID.randomUUID().toString(), correlations: CorrelationMap) = Task(
    id = id,
    sourceReference = ProcessReference(
      instanceId = UUID.randomUUID().toString(),
      applicationName = "test application",
      definitionId = "myProcess:1",
      definitionKey = "myProcess",
      executionId = UUID.randomUUID().toString(),
      name = "My Process",
      tenantId = null
    ),
    correlations = correlations,
    taskDefinitionKey = "task_1"
  )
}
