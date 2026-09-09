package io.holunda.polyflow.liquibase.autoconfigure

import org.springframework.boot.SpringApplication
import org.springframework.boot.env.EnvironmentPostProcessor
import org.springframework.core.Ordered
import org.springframework.core.env.ConfigurableEnvironment
import org.springframework.core.env.MapPropertySource

/**
 * Prevents the normal Liquibase updater and application server from starting
 * when the explicit one-shot adoption mode is enabled.
 */
class PolyflowLiquibaseAdoptionEnvironmentPostProcessor : EnvironmentPostProcessor, Ordered {

  companion object {
    const val ADOPTION_ENABLED = "polyflow.liquibase.adoption.enabled"
    const val PROPERTY_SOURCE = "polyflowLiquibaseAdoption"
    val ADOPTION_PROPERTIES = mapOf(
      "spring.liquibase.enabled" to false,
      "spring.main.web-application-type" to "none"
    )
  }

  override fun postProcessEnvironment(environment: ConfigurableEnvironment, application: SpringApplication) {
    if (environment.getProperty(ADOPTION_ENABLED, Boolean::class.java, false)) {
      environment.propertySources.addFirst(MapPropertySource(PROPERTY_SOURCE, ADOPTION_PROPERTIES))
    }
  }

  override fun getOrder(): Int = Ordered.LOWEST_PRECEDENCE

}
