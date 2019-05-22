@file:Suppress("unused")

package io.holunda.camunda.taskpool

import io.holunda.camunda.taskpool.enricher.ProcessVariablesCorrelator
import io.holunda.camunda.taskpool.enricher.ProcessVariablesFilter
import io.holunda.camunda.taskpool.urlresolver.TasklistUrlResolver
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean

/**
 * No @Configuration required.
 * Configuration used via auto-configuration.
 */
@ConditionalOnMissingBean(ProcessVariablesFilter::class)
open class FallbackProcessVariablesFilterConfiguration {
  /**
   * Empty process variable filter.
   */
  @Bean
  open fun processVariablesFilterFallback(): ProcessVariablesFilter = ProcessVariablesFilter()
}

/**
 * No @Configuration required.
 * Configuration used via auto-configuration.
 */
@ConditionalOnMissingBean(ProcessVariablesCorrelator::class)
open class FallbackProcessVariablesCorrelatorConfiguration {
  /**
   * Empty correlator.
   */
  @Bean
  open fun processVariablesCorrelatorFallback(): ProcessVariablesCorrelator = ProcessVariablesCorrelator()
}


/**
 * No @Configuration required.
 * Configuration used via auto-configuration.
 */
@ConditionalOnMissingBean(TasklistUrlResolver::class)
open class FallbackTasklistUrlResolverConfiguration {

  /**
   * Property-based Tasklist URL resolver.
   */
  @Bean
  @ConditionalOnBean(TaskCollectorProperties::class)
  open fun propertyBasedTasklistUrlResolver(properties: TaskCollectorProperties): TasklistUrlResolver {
    return if (properties.tasklistUrl == null) {
      throw IllegalStateException("Either set camunda.taskpool.collector.tasklist-url property or provide own implementation asState TasklistUrlResolver")
    } else {
      object : TasklistUrlResolver {
        override fun getTasklistUrl(): String = properties.tasklistUrl!!
      }
    }
  }
}
