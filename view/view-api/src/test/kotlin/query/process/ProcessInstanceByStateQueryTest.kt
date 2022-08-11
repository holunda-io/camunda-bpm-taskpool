package io.holunda.polyflow.view.query.process

import io.holunda.camunda.taskpool.api.task.ProcessReference
import io.holunda.polyflow.view.ProcessInstance
import io.holunda.polyflow.view.ProcessInstanceState
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class ProcessInstanceByStateQueryTest {

  private val processInstance = ProcessInstance(
    processInstanceId = "9034278213",
    sourceReference = ProcessReference(
      instanceId = "instance-id",
      executionId = "exec-id",
      definitionId = "def-id",
      definitionKey = "def-key",
      name = "process name",
      applicationName = "test-application"
    ),
    state = ProcessInstanceState.RUNNING
  )

  @Test
  fun `should filter by state`() {
    assertThat(ProcessInstancesByStateQuery(states = setOf(ProcessInstanceState.RUNNING)).applyFilter(processInstance)).isTrue
    assertThat(ProcessInstancesByStateQuery(states = setOf(ProcessInstanceState.FINISHED)).applyFilter(processInstance)).isFalse
  }
}
