package io.holunda.polyflow.liquibase.autoconfigure

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.boot.SpringApplication
import org.springframework.core.env.MapPropertySource
import org.springframework.mock.env.MockEnvironment

class PolyflowLiquibaseAdoptionEnvironmentPostProcessorTest {

  private val postProcessor = PolyflowLiquibaseAdoptionEnvironmentPostProcessor()

  @Test
  fun `disables normal Liquibase startup and web application mode when adoption is enabled`() {
    val environment = MockEnvironment().apply {
      propertySources.addFirst(MapPropertySource("test", mapOf("polyflow.liquibase.adoption.enabled" to true)))
    }

    postProcessor.postProcessEnvironment(environment, SpringApplication())

    assertThat(environment.getProperty("spring.liquibase.enabled", Boolean::class.java)).isFalse()
    assertThat(environment.getProperty("spring.main.web-application-type")).isEqualTo("none")
  }

  @Test
  fun `leaves normal startup unchanged when adoption is disabled`() {
    val environment = MockEnvironment()

    postProcessor.postProcessEnvironment(environment, SpringApplication())

    assertThat(environment.getProperty("spring.liquibase.enabled")).isNull()
    assertThat(environment.getProperty("spring.main.web-application-type")).isNull()
  }
}
