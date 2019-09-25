package io.holunda.camunda.taskpool

import io.holunda.camunda.client.EnableCamundaEngineClient
import io.holunda.camunda.datapool.EnableDataEntryCollector
import io.holunda.camunda.taskpool.plugin.EnableCamundaSpringEventing
import org.springframework.context.annotation.Configuration

/**
 * Configuration enabling engine components.
 */
@EnableCamundaSpringEventing
@EnableCamundaEngineClient
@EnableTaskCollector
@EnableDataEntryCollector
@Configuration
class TaskpoolEngineSupportConfiguration
