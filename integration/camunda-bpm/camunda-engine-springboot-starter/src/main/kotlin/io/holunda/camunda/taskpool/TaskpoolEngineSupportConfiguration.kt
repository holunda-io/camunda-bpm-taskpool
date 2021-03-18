package io.holunda.camunda.taskpool

import io.holunda.camunda.client.EnableCamundaEngineClient
import io.holunda.camunda.datapool.EnableDataEntryCollector
import org.springframework.context.annotation.Configuration

/**
 * Configuration enabling engine components.
 */
@EnableCamundaEngineClient
@EnableTaskCollector
@EnableDataEntryCollector
@Configuration
class TaskpoolEngineSupportConfiguration
