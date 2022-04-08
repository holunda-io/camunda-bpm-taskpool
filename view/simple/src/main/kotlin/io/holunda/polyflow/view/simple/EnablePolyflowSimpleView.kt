package io.holunda.polyflow.view.simple

import org.springframework.context.annotation.Import


/**
 * Enables simple (in-memory) polyflow view.
 */
@MustBeDocumented
@Import(TaskPoolSimpleViewConfiguration::class)
annotation class EnablePolyflowSimpleView
