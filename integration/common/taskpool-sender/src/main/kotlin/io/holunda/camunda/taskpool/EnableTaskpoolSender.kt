package io.holunda.camunda.taskpool

import io.holunda.camunda.taskpool.sender.SenderConfiguration
import org.springframework.context.annotation.Import

/**
 * Enables the taskpool sender.
 */
@MustBeDocumented
@Import(SenderConfiguration::class)
annotation class EnableTaskpoolSender
