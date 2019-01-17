package io.holunda.camunda.taskpool.view

import io.holunda.camunda.taskpool.api.business.CorrelationMap
import io.holunda.camunda.taskpool.api.business.addCorrelation
import io.holunda.camunda.taskpool.api.business.dataIdentity
import io.holunda.camunda.taskpool.api.business.newCorrelations
import io.holunda.camunda.taskpool.api.task.ProcessReference
import org.assertj.core.api.Assertions.assertThat
import org.camunda.bpm.engine.variable.Variables
import org.junit.Test
import java.util.*

class TaskWithDataEntriesTest {

  @Test
  fun `should correlate data entries`() {

    val entries = listOf(
      DataEntry(
        entryType = "EntryType1",
        entryId = UUID.randomUUID().toString(),
        payload = Variables.putValue("foo", "bar")
      ),
      DataEntry(
        entryType = "EntryType2",
        entryId = UUID.randomUUID().toString(),
        payload = Variables.putValue("zee", "other")
      )
    )

    val correlations: CorrelationMap = newCorrelations()
    val dataEntries: MutableMap<String, DataEntry> = mutableMapOf()

    // add all to the data entries and provide correlations
    entries.forEach {
      dataEntries[dataIdentity(entryType = it.entryType, entryId = it.entryId)] = it
      correlations.addCorrelation(entryType = it.entryType, entryId = it.entryId)
    }

    val task = makeTask(UUID.randomUUID().toString(), correlations)

    val taskWithDataEntries = tasksWithDataEntries(task, dataEntries)

    assertThat(taskWithDataEntries.dataEntries).containsExactlyElementsOf(entries)
  }


  private fun makeTask(id: String, correlations: CorrelationMap) = Task(
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
