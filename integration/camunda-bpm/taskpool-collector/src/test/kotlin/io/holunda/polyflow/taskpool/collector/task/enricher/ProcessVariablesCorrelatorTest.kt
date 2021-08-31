package io.holunda.polyflow.taskpool.collector.task.enricher

import org.camunda.bpm.engine.variable.Variables
import org.camunda.bpm.model.xml.test.assertions.ModelAssertions.assertThat
import org.junit.Test

class ProcessVariablesCorrelatorTest {

  private val variables = Variables.createVariables()
    .putValueTyped("business_var1", Variables.stringValue("1"))
    .putValueTyped("business_var2", Variables.stringValue("2"))
    .putValueTyped("business_var3", Variables.stringValue("3"))
    .putValueTyped("business_var4", Variables.stringValue("4"))
    .putValueTyped("business_var12", Variables.stringValue("12"))
    .putValueTyped("business_var32", Variables.stringValue("32"))
    .putValueTyped("business_var42", Variables.stringValue("42"))
    .putValueTyped("business_var51", Variables.stringValue("51"))


  @Test
  fun `should correlate on task level`() {
    val correlator = ProcessVariablesCorrelator(
      ProcessVariableCorrelation("process_key", mapOf(
        "task1" to mapOf(
          "business_var2" to "MyCorrelationId",
          "business_var3" to "MyOtherCorrelationId",
          "business_var47" to "Will be empty"
        )
      ))
    )

    assertThat(correlator.correlateVariables("process_key", "task1", variables)["MyCorrelationId"]).isEqualTo("2")
    assertThat(correlator.correlateVariables("process_key", "task1", variables)["MyOtherCorrelationId"]).isEqualTo("3")
    assertThat(correlator.correlateVariables("process_key", "task1", variables).containsKey("Will be empty")).isFalse()
  }

  @Test
  fun `should correlate on process level (global)`() {
    val correlator = ProcessVariablesCorrelator(
      ProcessVariableCorrelation("process_key",
        emptyMap(),
        mapOf(
          "business_var2" to "MyCorrelationId",
          "business_var3" to "MyOtherCorrelationId",
          "business_var47" to "Will be empty"
        )
      )
    )

    assertThat(correlator.correlateVariables("process_key", "task56", variables)["MyCorrelationId"]).isEqualTo("2")
    assertThat(correlator.correlateVariables("process_key", "task67", variables)["MyOtherCorrelationId"]).isEqualTo("3")
    assertThat(correlator.correlateVariables("process_key", "task121", variables).containsKey("Will be empty")).isFalse()
  }

}
