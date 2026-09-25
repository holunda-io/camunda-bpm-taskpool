package io.holunda.polyflow.taskpool

import io.holunda.polyflow.taskpool.collector.ProcessEngineApiTaskpoolCollectorConfiguration
import org.springframework.context.annotation.Import

/**
 * Enables the task collector, registering the task subscription and performs, collecting, enriching and sending
 * of taskpool commands to Task Pool Core.
 */
@MustBeDocumented
@Import(ProcessEngineApiTaskpoolCollectorConfiguration::class)
@EnableTaskpoolSender
annotation class EnableProcessEngineApiTaskpoolCollector
