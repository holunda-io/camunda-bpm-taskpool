package io.holunda.camunda.taskpool

import io.holunda.camunda.client.EnableCamundaEngineClient
import io.holunda.camunda.datapool.EnableDataEntrySender
import org.springframework.context.annotation.Configuration

/**
 * Configuration enabling engine components.
 */
@EnableCamundaEngineClient
@EnableCamundaTaskpoolCollector
@EnableDataEntrySender
@Configuration
class TaskpoolEngineSupportConfiguration
