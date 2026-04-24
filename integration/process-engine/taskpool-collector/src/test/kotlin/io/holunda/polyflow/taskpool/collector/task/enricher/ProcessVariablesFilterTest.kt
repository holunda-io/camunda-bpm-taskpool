package io.holunda.polyflow.taskpool.collector.task.enricher

import org.camunda.bpm.engine.variable.Variables
import org.camunda.bpm.engine.variable.Variables.stringValue
import org.camunda.bpm.model.xml.test.assertions.ModelAssertions.assertThat
import org.junit.jupiter.api.Test

class ProcessVariablesFilterTest {

  private val variables = Variables.createVariables()
    .putValueTyped("business_var1", stringValue("1"))
    .putValueTyped("business_var2", stringValue("2"))
    .putValueTyped("business_var3", stringValue("3"))
    .putValueTyped("business_var4", stringValue("4"))
    .putValueTyped("business_var12", stringValue("12"))
    .putValueTyped("business_var32", stringValue("32"))
    .putValueTyped("business_var42", stringValue("42"))
    .putValueTyped("business_var51", stringValue("51"))

  @Test
  fun `should filter for task`() {
    val filter = ProcessVariablesFilter(
      TaskVariableFilter("process7411", FilterType.INCLUDE, mapOf(
        "task1" to listOf("business_var1", "business_var2"),
        "task2" to listOf("business_var3", "business_var2")
      )),
      TaskVariableFilter("process7412", FilterType.EXCLUDE, mapOf(
        "task3" to listOf("business_var51", "business_var42"),
        "task1" to listOf("business_var12", "business_var32")
      ))
    )

    // verify filter for task1 of process7411
    assertThat(filter.filterVariables("process7411", "task1", variables)).containsOnlyKeys("business_var1", "business_var2")
    // verify filter for task2 of process7411
    assertThat(filter.filterVariables("process7411", "task2", variables)).containsOnlyKeys("business_var3", "business_var2")
    // verify that no filter is applied for task3 of process7411
    assertThat(filter.filterVariables("process7411", "task3", variables))
      .containsOnlyKeys("business_var1", "business_var2", "business_var3", "business_var4", "business_var12", "business_var32", "business_var42", "business_var51")

    // verify filter for task3 of process7412
    assertThat(filter.filterVariables("process7412", "task3", variables)).containsOnlyKeys("business_var1", "business_var2", "business_var3", "business_var4", "business_var12", "business_var32")
    // verify filter for task1 of process7412
    assertThat(filter.filterVariables("process7412", "task1", variables)).containsOnlyKeys("business_var1", "business_var2", "business_var3", "business_var4", "business_var51", "business_var42")
    // verify that no filter is applied for task2 of process7412
    assertThat(filter.filterVariables("process7412", "task2", variables))
      .containsOnlyKeys("business_var1", "business_var2", "business_var3", "business_var4", "business_var12", "business_var32", "business_var42", "business_var51")
  }

  @Test
  fun `should filter for specific process`() {
    val filter = ProcessVariablesFilter(
      ProcessVariableFilter("process7411", FilterType.INCLUDE, listOf("business_var1", "business_var2")),
      ProcessVariableFilter("process7412", FilterType.EXCLUDE, listOf("business_var51", "business_var42", "business_var32", "business_var12"))
    )

    // for process7411: only 1, 2
    assertThat(filter.filterVariables("process7411", "task77", variables)).containsOnlyKeys("business_var1", "business_var2")

    // for process7412: not 12, 32, 42, 52 -> 1, 2, 3, 4
    assertThat(filter.filterVariables("process7412", "task88", variables)).containsOnlyKeys("business_var1", "business_var2", "business_var3", "business_var4")

    // for process7413: all variables
    assertThat(filter.filterVariables("process7413", "task88", variables))
      .containsOnlyKeys("business_var1", "business_var2", "business_var3", "business_var4", "business_var12", "business_var32", "business_var42", "business_var51")
  }

  @Test
  fun `should filter for all processes`() {
    val filter = ProcessVariablesFilter(
      ProcessVariableFilter(FilterType.INCLUDE, listOf("business_var1", "business_var2")),
      ProcessVariableFilter("process7412", FilterType.INCLUDE, listOf("business_var1", "business_var3", "business_var4"))
    )

    // process without dedicated filter -> use common filter -> include only 1, 2
    assertThat(filter.filterVariables("process7411", "task77", variables)).containsOnlyKeys("business_var1", "business_var2")

    // process with dedicated filter -> include only 1, 3, 4
    assertThat(filter.filterVariables("process7412", "task88", variables)).containsOnlyKeys("business_var1", "business_var3", "business_var4")
  }
}
