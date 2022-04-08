package io.holunda.polyflow.taskpool

import io.holunda.polyflow.datapool.EnableDataEntrySender
import org.springframework.context.annotation.Configuration

/**
 * Configuration enabling engine components.
 */
@EnableCamundaTaskpoolCollector
@EnableDataEntrySender
class TaskpoolEngineSupportConfiguration
