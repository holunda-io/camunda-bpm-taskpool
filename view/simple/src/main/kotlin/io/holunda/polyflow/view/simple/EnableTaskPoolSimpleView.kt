package io.holunda.polyflow.view.simple

import org.springframework.context.annotation.Import

/**
 * Enables simple taskpool view.
 */
@MustBeDocumented
@Deprecated(message = "Please use EnablePolyflowSimpleView instead", replaceWith = ReplaceWith("EnablePolyflowSimpleView"))
@Import(TaskPoolSimpleViewConfiguration::class)
annotation class EnableTaskPoolSimpleView

/**
 * Enables simple (in-memory) polyflow view.
 */
@MustBeDocumented
@Import(TaskPoolSimpleViewConfiguration::class)
annotation class EnablePolyflowSimpleView
