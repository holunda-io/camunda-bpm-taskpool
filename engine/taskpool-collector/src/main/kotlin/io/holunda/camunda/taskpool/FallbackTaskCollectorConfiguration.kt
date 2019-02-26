@file:Suppress("unused")

package io.holunda.camunda.taskpool

import io.holunda.camunda.taskpool.enricher.ProcessVariablesCorrelator
import io.holunda.camunda.taskpool.enricher.ProcessVariablesFilter
import io.holunda.camunda.taskpool.urlresolver.TasklistUrlResolver
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean

/**
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
 * Configuration used via auto-configuration.
 */
@ConditionalOnMissingBean(TasklistUrlResolver::class)
open class FallbackTasklistUrlResolverConfiguration {
  /**
   * Property-based Tasklist URL resolver.
   */
  @Bean
  @ConditionalOnMissingBean
  open fun propertyBasedTasklistUrlResolver(properties: TaskCollectorProperties): TasklistUrlResolver {
    return if (properties.tasklistUrl == null) {
      throw IllegalStateException("Either set camunda.taskpool.collector.tasklist-url property or provide own implementation of TasklistUrlResolver")
    } else {
      object : TasklistUrlResolver {
        override fun getTasklistUrl(): String = properties.tasklistUrl!!
      }
    }
  }

}
