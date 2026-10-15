package io.holunda.polyflow.taskpool.collector.process.instance

import io.holunda.camunda.taskpool.api.process.instance.StartProcessInstanceCommand
import io.holunda.camunda.taskpool.api.task.ProcessReference
import io.holunda.polyflow.taskpool.collector.task.enricher.CorrelationDefinition
import io.holunda.polyflow.taskpool.collector.task.enricher.FilterType
import io.holunda.polyflow.taskpool.collector.task.enricher.ProcessVariableCorrelation
import io.holunda.polyflow.taskpool.collector.task.enricher.ProcessVariableFilter
import io.holunda.polyflow.taskpool.collector.task.enricher.ProcessVariablesCorrelator
import io.holunda.polyflow.taskpool.collector.task.enricher.ProcessVariablesFilter
import org.assertj.core.api.Assertions.assertThat
import org.camunda.bpm.engine.HistoryService
import org.camunda.bpm.engine.RuntimeService
import org.camunda.bpm.engine.variable.Variables
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class ProcessVariablesProcessInstanceCommandEnricherTest {

  @Test
  fun `uses the configured process filter and global correlations`() {
    val runtimeService = mock<RuntimeService>()
    whenever(runtimeService.getVariablesTyped("process-1", false)).thenReturn(
      Variables.fromMap(mapOf("visible" to "value", "requestId" to "42", "excluded" to "ignore"))
    )
    whenever(runtimeService.getVariablesTyped(eq("process-1"), any<Collection<String>>(), eq(true))).thenReturn(
      Variables.fromMap(mapOf("visible" to "value", "requestId" to "42"))
    )

    val command = StartProcessInstanceCommand("process-1", sourceReference())
    ProcessVariablesProcessInstanceCommandEnricher(
      ProcessVariablesFilter(ProcessVariableFilter("approval", FilterType.INCLUDE, listOf("visible"))),
      ProcessVariablesCorrelator(
        ProcessVariableCorrelation("approval", globalCorrelations = listOf(CorrelationDefinition("requestId", "request")))
      ),
      ProcessInstanceVariableLoader(runtimeService, mock<HistoryService>()),
    ).enrich(command)

    assertThat(command.payload).containsOnlyKeys("visible")
    assertThat(command.correlations).containsEntry("request", "42")
    assertThat(command.enriched).isTrue()
  }

  private fun sourceReference() = ProcessReference(
    "process-1", "process-1", "approval:1", "approval", "Approval", "example"
  )
}
