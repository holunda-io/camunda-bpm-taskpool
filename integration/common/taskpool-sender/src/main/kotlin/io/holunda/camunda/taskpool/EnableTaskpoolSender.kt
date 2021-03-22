package io.holunda.camunda.taskpool

import org.springframework.context.annotation.Import

/**
 * Enables the taskpool sender.
 */
@MustBeDocumented
@Import(SenderConfiguration::class)
annotation class EnableTaskpoolSender
