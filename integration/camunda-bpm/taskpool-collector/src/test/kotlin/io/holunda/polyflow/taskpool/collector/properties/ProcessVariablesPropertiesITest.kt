package io.holunda.polyflow.taskpool.collector.properties

import io.holunda.polyflow.taskpool.collector.CamundaTaskpoolCollectorProperties
import io.holunda.polyflow.taskpool.collector.task.enricher.ProcessVariablesCorrelator
import io.holunda.polyflow.taskpool.collector.task.enricher.ProcessVariablesFilter
import io.holunda.polyflow.taskpool.sender.SenderProperties
import org.assertj.core.api.Assertions.assertThat
import org.camunda.bpm.engine.variable.Variables
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContext
import org.springframework.test.context.ActiveProfiles

@SpringBootTest(
  classes = [ProcessVariablesPropertiesITest.PropertiesTestApplication::class],
  webEnvironment = SpringBootTest.WebEnvironment.MOCK
)
@ActiveProfiles("process-variables-properties-itest")
internal class ProcessVariablesPropertiesITest {

  @Autowired
  private lateinit var applicationContext: ApplicationContext

  @Autowired
  private lateinit var processVariablesFilter: ProcessVariablesFilter

  @Autowired
  private lateinit var processVariablesCorrelator: ProcessVariablesCorrelator

  @Test
  fun `initializes property-configured process variable filter and correlator`() {
    assertThat(applicationContext.containsBean("processVariablesFilter")).isTrue()
    assertThat(applicationContext.containsBean("processVariablesFilterFallback")).isFalse()
    assertThat(applicationContext.containsBean("processVariablesCorrelator")).isTrue()
    assertThat(applicationContext.containsBean("processVariablesCorrelatorFallback")).isFalse()

    val variables = Variables.fromMap(
      mapOf("requestId" to "42", "customerId" to "24", "internalAudit" to "hidden")
    )

    assertThat(processVariablesFilter.filterVariables("approval", "approve", variables))
      .containsOnlyKeys("requestId", "customerId")
    assertThat(processVariablesCorrelator.correlateVariables("approval", "approve", variables))
      .containsEntry("request", "42")
      .doesNotContainKey("customer")
  }

  @SpringBootApplication
  @EnableConfigurationProperties(CamundaTaskpoolCollectorProperties::class, SenderProperties::class)
  class PropertiesTestApplication
}
