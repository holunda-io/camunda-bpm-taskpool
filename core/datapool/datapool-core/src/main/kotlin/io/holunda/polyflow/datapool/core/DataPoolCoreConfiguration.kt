package io.holunda.polyflow.datapool.core

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

private val logger = KotlinLogging.logger {}

/**
 * Configuration of polyflow data pool core.
 */
@Configuration
@EnableConfigurationProperties(
  DataPoolProperties::class
)
class DataPoolCoreConfiguration {

  /**
   * Deletion strategy for lax handling of updates after deletion (default).
   */
  @ConditionalOnProperty(
    name = ["polyflow.core.data-entry.deletion-strategy"],
    havingValue = "lax",
    matchIfMissing = true
  )
  @Bean
  fun laxDeletionStrategy(): DeletionStrategy = object : DeletionStrategy {
    override fun strictMode(): Boolean = false
  }.also {
    logger.info { "Data pool core: using LAX deletion strategy." }
  }

  /**
   * Deletion strategy for strict handling of updates after deletion.
   */
  @ConditionalOnProperty(
    name = ["polyflow.core.data-entry.deletion-strategy"],
    havingValue = "strict",
    matchIfMissing = false
  )
  @Bean
  fun strictDeletionStrategy(): DeletionStrategy = object : DeletionStrategy {
    override fun strictMode(): Boolean = true
  }.also {
    logger.info { "Data pool core: using STRICT deletion strategy." }
  }
}

