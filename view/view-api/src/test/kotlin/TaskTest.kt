package io.holunda.polyflow.view

import io.holunda.camunda.taskpool.api.business.CorrelationMap
import io.holunda.camunda.taskpool.api.business.addCorrelation
import io.holunda.camunda.taskpool.api.business.newCorrelations
import io.holunda.camunda.taskpool.api.task.ProcessReference
import org.assertj.core.api.Assertions.assertThat
import org.camunda.bpm.engine.variable.Variables
import org.junit.jupiter.api.Test
import java.util.*

class TaskTest {

  @Test
  fun `get correlationIdentities`() {
    val dataEntry1 = DataEntry(entryType = "A", entryId = "1", payload = Variables.putValue("x", "y"), name = "A1", type = "A", applicationName = "y")
    val dataEntry2 = DataEntry(entryType = "A", entryId = "1", payload = Variables.putValue("x", "y"), name = "A1", type = "A", applicationName = "y")

    val task = createTask("0", correlationMap(dataEntry1, dataEntry2))

    assertThat(task.correlationIdentities).containsOnly("A#1")
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
