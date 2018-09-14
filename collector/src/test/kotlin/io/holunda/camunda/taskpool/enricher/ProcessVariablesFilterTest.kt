package io.holunda.camunda.taskpool.enricher

import org.camunda.bpm.engine.variable.Variables
import org.camunda.bpm.engine.variable.Variables.stringValue
import org.camunda.bpm.model.xml.test.assertions.ModelAssertions.assertThat
import org.junit.Test

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
  fun checkFilter() {
    val filter: ProcessVariablesFilter = ProcessVariablesFilter(
      ProcessVariableFilter("process7411", FilterType.INCLUDE, mapOf(
        "task1" to listOf("business_var1", "business_var2"),
        "task2" to listOf("business_var3", "business_var2")
      )),
      ProcessVariableFilter("process7412", FilterType.EXCLUDE, mapOf(
        "task3" to listOf("business_var51", "business_var42"),
        "task1" to listOf("business_var12", "business_var32")
      ))
    )

    assertThat(filter.filterVariables("process7411", "task1", variables)).containsOnlyKeys("business_var1", "business_var2")
    assertThat(filter.filterVariables("process7411", "task2", variables)).containsOnlyKeys("business_var3", "business_var2")
    assertThat(filter.filterVariables("process7412", "task3", variables)).containsOnlyKeys("business_var1", "business_var2", "business_var3", "business_var4", "business_var12", "business_var32")
    assertThat(filter.filterVariables("process7412", "task1", variables)).containsOnlyKeys("business_var1", "business_var2", "business_var3", "business_var4", "business_var51", "business_var42")
  }
}
