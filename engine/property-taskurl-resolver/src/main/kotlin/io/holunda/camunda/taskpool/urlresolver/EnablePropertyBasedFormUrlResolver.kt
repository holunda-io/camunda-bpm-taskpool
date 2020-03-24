package io.holunda.camunda.taskpool.urlresolver

import org.springframework.context.annotation.Import

/**
 * Annotation to enable property-based form url resolver component.
 */
@MustBeDocumented
@Import(PropertyBasedFormUrlResolverConfiguration::class)
annotation class EnablePropertyBasedFormUrlResolver
