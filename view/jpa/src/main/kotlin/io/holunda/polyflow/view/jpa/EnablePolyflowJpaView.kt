package io.holunda.polyflow.view.jpa

import org.springframework.context.annotation.Import

/**
 * Enables polyflow projection using RDMBS via JPA as persistence.
 */
@MustBeDocumented
@Import(PolyflowJpaViewConfiguration::class)
annotation class EnablePolyflowJpaView
