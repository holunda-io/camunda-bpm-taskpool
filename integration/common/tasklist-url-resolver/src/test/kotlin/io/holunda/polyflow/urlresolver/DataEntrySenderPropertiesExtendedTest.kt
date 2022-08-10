package io.holunda.polyflow.urlresolver

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.springframework.boot.autoconfigure.AutoConfigurations
import org.springframework.boot.context.annotation.UserConfigurations
import org.springframework.boot.test.context.runner.ApplicationContextRunner
import org.springframework.context.annotation.Bean

class DataEntrySenderPropertiesExtendedTest {

  private val contextRunner = ApplicationContextRunner()


  @Test
  fun testMinimal() {
    contextRunner
      .withUserConfiguration(TestMockConfiguration::class.java)
      .withConfiguration(AutoConfigurations.of(FallbackTasklistUrlResolverAutoConfiguration::class.java))
      .run {

        assertThat(it.getBean(TasklistUrlProperties::class.java)).isNotNull
        val props: TasklistUrlProperties = it.getBean(TasklistUrlProperties::class.java)

        assertThat(props.tasklistUrl).isNull()
      }
  }

  @Test
  fun testAllChanged() {
    contextRunner
      .withConfiguration(UserConfigurations.of(FallbackTasklistUrlResolverAutoConfiguration::class.java))
      .withPropertyValues(
        "polyflow.integration.tasklist.tasklist-url=http://some",
      ).run {

        assertThat(it.getBean(TasklistUrlProperties::class.java)).isNotNull
        val props: TasklistUrlProperties = it.getBean(TasklistUrlProperties::class.java)

        assertThat(props.tasklistUrl).isEqualTo("http://some")
      }
  }


  class TestMockConfiguration {

    @Bean
    fun ownResolver(): TasklistUrlResolver = mock()
  }
}
