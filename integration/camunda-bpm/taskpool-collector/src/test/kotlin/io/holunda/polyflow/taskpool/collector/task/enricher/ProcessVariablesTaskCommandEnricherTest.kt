package io.holunda.polyflow.taskpool.collector.task.enricher

import io.holunda.camunda.taskpool.api.task.CreateTaskCommand
import io.holunda.camunda.taskpool.api.task.ProcessReference
import io.holunda.polyflow.taskpool.collector.task.TaskVariableLoader
import org.assertj.core.api.Assertions.assertThat
import org.camunda.bpm.engine.RuntimeService
import org.camunda.bpm.engine.TaskService
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor
import org.camunda.bpm.engine.variable.Variables
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class ProcessVariablesTaskCommandEnricherTest {

  @Test
  fun `deserializes only payload and correlation variables when a filter restricts the task`() {
    val taskService = mock<TaskService>()
    val availableVariables = Variables.fromMap(mapOf("included" to "value", "correlationId" to "42", "excluded" to "serialized"))
    val selectedVariables = Variables.fromMap(mapOf("included" to "value", "correlationId" to "42"))
    whenever(taskService.getVariablesTyped("task-1", false)).thenReturn(availableVariables)
    whenever(taskService.getVariablesTyped(eq("task-1"), any<Collection<String>>(), eq(true))).thenReturn(selectedVariables)

    val command = command()
    enricher(
      taskService = taskService,
      filter = ProcessVariablesFilter(ProcessVariableFilter("approval", FilterType.INCLUDE, listOf("included"))),
      correlator = ProcessVariablesCorrelator(
        ProcessVariableCorrelation("approval", globalCorrelations = listOf(CorrelationDefinition("correlationId", "request")))
      )
    ).enrich(command)

    val requestedNames = collectionCaptor()
    verify(taskService).getVariablesTyped(eq("task-1"), requestedNames.capture(), eq(true))
    assertThat(requestedNames.firstValue).containsExactlyInAnyOrder("included", "correlationId")
    assertThat(command.payload).containsOnlyKeys("included")
    assertThat(command.correlations).containsEntry("request", "42")
    verify(taskService, never()).getVariablesTyped("task-1", true)
  }

  @Test
  fun `uses one deserialized read when no filter restricts the task`() {
    val taskService = mock<TaskService>()
    whenever(taskService.getVariablesTyped("task-1", true)).thenReturn(Variables.fromMap(mapOf("included" to "value")))

    enricher(taskService, ProcessVariablesFilter(), ProcessVariablesCorrelator()).enrich(command())

    verify(taskService).getVariablesTyped("task-1", true)
    verify(taskService, never()).getVariablesTyped("task-1", false)
    verify(taskService, never()).getVariablesTyped(eq("task-1"), any<Collection<String>>(), eq(true))
  }

  private fun enricher(
    taskService: TaskService,
    filter: ProcessVariablesFilter,
    correlator: ProcessVariablesCorrelator
  ) = ProcessVariablesTaskCommandEnricher(
    processVariablesFilter = filter,
    processVariablesCorrelator = correlator,
    taskVariableLoader = TaskVariableLoader(mock<RuntimeService>(), taskService, mock<CommandExecutor>())
  )

  private fun command() = CreateTaskCommand(
    id = "task-1",
    sourceReference = ProcessReference("process-1", "execution-1", "approval:1", "approval", "Approval", "example"),
    taskDefinitionKey = "review"
  )

  private fun collectionCaptor() = argumentCaptor<Collection<String>>()
}
