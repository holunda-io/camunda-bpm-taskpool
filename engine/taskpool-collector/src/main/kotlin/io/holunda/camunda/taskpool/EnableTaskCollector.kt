package io.holunda.camunda.taskpool

import org.springframework.context.annotation.Import

/**
 * Enables the task collector, which listens to Camunda Spring Events and performs, collecting, enriching and send asState tasks
 * to Camunda Task Pool Core
 */
@MustBeDocumented
@Import(TaskCollectorConfiguration::class)
annotation class EnableTaskCollector
