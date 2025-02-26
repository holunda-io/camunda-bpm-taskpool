package io.holunda.polyflow.view.jpa

import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.annotation.PostConstruct
import org.axonframework.eventhandling.deadletter.jpa.DeadLetterEntry
import org.axonframework.eventhandling.tokenstore.jpa.TokenEntry
import org.axonframework.modelling.saga.repository.jpa.SagaEntry
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

private val logger = KotlinLogging.logger {}

/**
 * From here and below, scan for components, entities and JPA repositories.
 */
@EnableConfigurationProperties(PolyflowJpaViewProperties::class)
@ComponentScan
@EntityScan(
  basePackageClasses = [
    PolyflowJpaViewConfiguration::class,
    // for the token
    TokenEntry::class,
    // we are a projection, Sagas might be needed too.
    SagaEntry::class,
    // Dead letter
    DeadLetterEntry::class
  ]
)
@EnableJpaRepositories(
  basePackageClasses = [
    PolyflowJpaViewConfiguration::class,
  ]
)
@Configuration
class PolyflowJpaViewConfiguration(
  val polyflowJpaViewProperties: PolyflowJpaViewProperties
) {

  /**
   * Logs a little.
   */
  @PostConstruct
  fun info() {
    logger.info { "VIEW-JPA-001: Initialized JPA view, storing items: ${polyflowJpaViewProperties.storedItems.joinToString(", ") { it.name }}" }
  }
}
