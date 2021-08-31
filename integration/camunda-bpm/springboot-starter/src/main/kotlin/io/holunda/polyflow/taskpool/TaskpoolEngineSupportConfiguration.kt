package io.holunda.polyflow.taskpool

import io.holunda.polyflow.client.camunda.EnableCamundaEngineClient
import io.holunda.polyflow.datapool.EnableDataEntrySender
import org.springframework.context.annotation.Configuration

/**
 * Configuration enabling engine components.
 */
@EnableCamundaEngineClient
@EnableCamundaTaskpoolCollector
@EnableDataEntrySender
@Configuration
class TaskpoolEngineSupportConfiguration
