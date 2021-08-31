package io.holunda.polyflow.taskpool

import io.holunda.polyflow.taskpool.sender.SenderConfiguration
import org.springframework.context.annotation.Import

/**
 * Enables the taskpool sender.
 */
@MustBeDocumented
@Import(SenderConfiguration::class)
annotation class EnableTaskpoolSender
