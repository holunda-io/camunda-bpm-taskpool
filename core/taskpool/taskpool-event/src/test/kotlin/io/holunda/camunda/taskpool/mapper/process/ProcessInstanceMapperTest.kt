package io.holunda.camunda.taskpool.mapper.process

import io.holunda.camunda.taskpool.api.business.addCorrelation
import io.holunda.camunda.taskpool.api.business.newCorrelations
import io.holunda.camunda.taskpool.api.process.instance.CancelProcessInstanceCommand
import io.holunda.camunda.taskpool.api.process.instance.FinishProcessInstanceCommand
import io.holunda.camunda.taskpool.api.process.instance.ResumeProcessInstanceCommand
import io.holunda.camunda.taskpool.api.process.instance.StartProcessInstanceCommand
import io.holunda.camunda.taskpool.api.process.instance.SuspendProcessInstanceCommand
import io.holunda.camunda.taskpool.api.task.ProcessReference
import org.assertj.core.api.Assertions.assertThat
import org.camunda.bpm.engine.variable.Variables
import org.junit.jupiter.api.Test

class ProcessInstanceMapperTest {

  @Test
  fun `preserves enrichment for every process lifecycle event`() {
    val payload = Variables.fromMap(mapOf("payload" to "value"))
    val correlations = newCorrelations().addCorrelation("request", "42")
    val sourceReference = ProcessReference("process-1", "process-1", "approval:1", "approval", "Approval", "example")

    val events = listOf(
      StartProcessInstanceCommand("process-1", sourceReference, payload = payload, correlations = correlations).startedEvent(),
      FinishProcessInstanceCommand("process-1", sourceReference, endActivityId = null, payload = payload, correlations = correlations).finishedEvent(),
      CancelProcessInstanceCommand("process-1", sourceReference, endActivityId = null, deleteReason = null, payload = payload, correlations = correlations).cancelledEvent(),
      SuspendProcessInstanceCommand("process-1", sourceReference, payload = payload, correlations = correlations).suspendedEvent(),
      ResumeProcessInstanceCommand("process-1", sourceReference, payload = payload, correlations = correlations).resumedEvent(),
    )

    events.forEach { event ->
      assertThat(event.payload).containsEntry("payload", "value")
      assertThat(event.correlations).containsEntry("request", "42")
    }
  }
}
