package io.holunda.polyflow.taskpool.collector

import io.holunda.polyflow.taskpool.collector.task.enricher.ProcessVariablesCorrelator
import io.holunda.polyflow.taskpool.collector.task.enricher.ProcessVariablesFilter
import org.assertj.core.api.Assertions.assertThat
import org.camunda.bpm.engine.variable.Variables
import org.junit.jupiter.api.Test
import org.springframework.boot.autoconfigure.AutoConfigurations
import org.springframework.boot.context.annotation.UserConfigurations
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.test.context.runner.ApplicationContextRunner
import org.springframework.context.annotation.Configuration

internal class ProcessVariablesPropertiesConfigurationTest {

  private val contextRunner = ApplicationContextRunner().withConfiguration(
    AutoConfigurations.of(
      ProcessVariablesFilterConfiguration::class.java,
      ProcessVariablesCorrelatorConfiguration::class.java,
      FallbackProcessVariablesFilterConfiguration::class.java,
      FallbackProcessVariablesCorrelatorConfiguration::class.java
    )
  ).withConfiguration(UserConfigurations.of(PropertiesConfiguration::class.java))

  @Configuration(proxyBeanMethods = false)
  @EnableConfigurationProperties(CamundaTaskpoolCollectorProperties::class)
  private class PropertiesConfiguration

  @Test
  fun `uses a property-configured process variables filter instead of fallback`() {
    contextRunner.withPropertyValues(
      "polyflow.integration.collector.camunda.task.enricher.process-variables-filter.enabled=true",
      "polyflow.integration.collector.camunda.task.enricher.process-variables-filter.filters[0].process-definition-key=approval",
      "polyflow.integration.collector.camunda.task.enricher.process-variables-filter.filters[0].filter-type=include",
      "polyflow.integration.collector.camunda.task.enricher.process-variables-filter.filters[0].process-variables=requestId"
    ).run { context ->
      assertThat(context).hasSingleBean(ProcessVariablesFilter::class.java)
      assertThat(context).hasBean("processVariablesFilter")
      assertThat(context).doesNotHaveBean("processVariablesFilterFallback")

      val filtered = context.getBean(ProcessVariablesFilter::class.java)
        .filterVariables("approval", "approve", Variables.fromMap(mapOf("requestId" to "42", "internal" to "hidden")))

      assertThat(filtered).containsOnlyKeys("requestId")
    }
  }

  @Test
  fun `uses a property-configured task variables filter`() {
    contextRunner.withPropertyValues(
      "polyflow.integration.collector.camunda.task.enricher.process-variables-filter.enabled=true",
      "polyflow.integration.collector.camunda.task.enricher.process-variables-filter.filters[0].process-definition-key=approval",
      "polyflow.integration.collector.camunda.task.enricher.process-variables-filter.filters[0].filter-type=include",
      "polyflow.integration.collector.camunda.task.enricher.process-variables-filter.filters[0].task-variables.approve[0]=requestId"
    ).run { context ->
      val filter = context.getBean(ProcessVariablesFilter::class.java)

      assertThat(filter.filterVariables("approval", "approve", Variables.fromMap(mapOf("requestId" to "42", "internal" to "hidden"))))
        .containsOnlyKeys("requestId")
      assertThat(filter.filterVariables("approval", "other", Variables.fromMap(mapOf("requestId" to "42", "internal" to "visible"))))
        .containsOnlyKeys("requestId", "internal")
    }
  }

  @Test
  fun `uses a property-configured process variables correlator instead of fallback`() {
    contextRunner.withPropertyValues(
      "polyflow.integration.collector.camunda.task.enricher.process-variables-correlator.enabled=true",
      "polyflow.integration.collector.camunda.task.enricher.process-variables-correlator.correlations[0].process-definition-key=approval",
      "polyflow.integration.collector.camunda.task.enricher.process-variables-correlator.correlations[0].global-correlations[0].entry-id-variable-name=requestId",
      "polyflow.integration.collector.camunda.task.enricher.process-variables-correlator.correlations[0].global-correlations[0].entry-type=request",
      "polyflow.integration.collector.camunda.task.enricher.process-variables-correlator.correlations[0].correlations.approve[0].entry-id-variable-name=customerId",
      "polyflow.integration.collector.camunda.task.enricher.process-variables-correlator.correlations[0].correlations.approve[0].entry-type=customer"
    ).run { context ->
      assertThat(context).hasSingleBean(ProcessVariablesCorrelator::class.java)
      assertThat(context).hasBean("processVariablesCorrelator")
      assertThat(context).doesNotHaveBean("processVariablesCorrelatorFallback")

      val correlations = context.getBean(ProcessVariablesCorrelator::class.java).correlateVariables(
        "approval", "approve", Variables.fromMap(mapOf("requestId" to "42", "customerId" to "24"))
      )

      assertThat(correlations).containsEntry("request", "42").containsEntry("customer", "24")
    }
  }

  @Test
  fun `uses a global-only property-configured correlation`() {
    contextRunner.withPropertyValues(
      "polyflow.integration.collector.camunda.task.enricher.process-variables-correlator.enabled=true",
      "polyflow.integration.collector.camunda.task.enricher.process-variables-correlator.correlations[0].process-definition-key=approval",
      "polyflow.integration.collector.camunda.task.enricher.process-variables-correlator.correlations[0].global-correlations[0].entry-id-variable-name=requestId",
      "polyflow.integration.collector.camunda.task.enricher.process-variables-correlator.correlations[0].global-correlations[0].entry-type=request"
    ).run { context ->
      assertThat(context).hasNotFailed()

      val correlations = context.getBean(ProcessVariablesCorrelator::class.java).correlateVariables(
        "approval", "approve", Variables.fromMap(mapOf("requestId" to "42"))
      )

      assertThat(correlations).containsEntry("request", "42")
    }
  }
}
