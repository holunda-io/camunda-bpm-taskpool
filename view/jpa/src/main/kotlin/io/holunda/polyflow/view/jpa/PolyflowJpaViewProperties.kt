package io.holunda.polyflow.view.jpa

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

/**
 * Properties to configure JPA View.
 */
@ConstructorBinding
@ConfigurationProperties(prefix = "polyflow.view.jpa")
data class PolyflowJpaViewProperties(
  val payloadAttributeLevelLimit: Int = -1
)
