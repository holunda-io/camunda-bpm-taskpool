package io.holunda.camunda.taskpool.sender

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.ComponentScan

/**
 * Configuration enabled by the property
 */
@ComponentScan
@ConditionalOnProperty(name = ["camunda.taskpool.collector.sender.type"], havingValue = "simple")
open class TaskCommandSenderConfiguration
