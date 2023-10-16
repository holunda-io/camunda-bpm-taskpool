package io.holunda.polyflow.spring

import io.holunda.polyflow.spring.ApplicationNameBeanPostProcessor.Companion.UNSET_APPLICATION_NAME
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.boot.context.annotation.UserConfigurations
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.test.context.runner.ApplicationContextRunner
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import

internal class ApplicationNameBeanPostProcessorTest {
  private val contextRunner = ApplicationContextRunner()
    .withConfiguration(UserConfigurations.of(TestConfig::class.java))

  @Test
  fun `sets application name if none is set`() {
    contextRunner
      .withPropertyValues(
        "spring.application.name=my-test-application"
      ).run {
        val testProperties = it.getBean(TestProperties::class.java)
        assertThat(testProperties).isNotNull
        assertThat(testProperties.applicationName).isEqualTo("my-test-application")
      }
  }

  @Test
  fun `leaves application name if specifically set`() {
    contextRunner
      .withPropertyValues(
        "spring.application.name=my-test-application",
        "polyflow.test.application-name=my-polyflow-test-application"
      ).run {
        val testProperties = it.getBean(TestProperties::class.java)
        assertThat(testProperties).isNotNull
        assertThat(testProperties.applicationName).isEqualTo("my-polyflow-test-application")
      }
  }

  @Test
  fun `leaves application name at default if spring-application-name is not set`() {
    contextRunner
      .run {
        val testProperties = it.getBean(TestProperties::class.java)
        assertThat(testProperties).isNotNull
        assertThat(testProperties.applicationName).isEqualTo(UNSET_APPLICATION_NAME)
      }
  }
}

@Configuration
@EnableConfigurationProperties(TestProperties::class)
@Import(ApplicationNameBeanPostProcessor::class)
class TestConfig

@ConfigurationProperties(prefix = "polyflow.test")
data class TestProperties(var applicationName: String = UNSET_APPLICATION_NAME)
