package io.holunda.polyflow.client.camunda

import org.mockito.kotlin.mock
import org.assertj.core.api.Assertions
import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.eventhandling.EventBus
import org.axonframework.serialization.Serializer
import org.axonframework.serialization.xml.XStreamSerializer
import org.camunda.bpm.engine.RuntimeService
import org.camunda.bpm.engine.TaskService
import org.junit.Test
import org.springframework.boot.context.annotation.UserConfigurations
import org.springframework.boot.test.context.runner.ApplicationContextRunner
import org.springframework.context.annotation.Bean

class CamundaEngineClientPropertiesExtendedTest {

  private val contextRunner = ApplicationContextRunner()
    .withConfiguration(UserConfigurations.of(CamundaEngineClientConfiguration::class.java))

  @Test
  fun testMinimal() {
    contextRunner
      .withUserConfiguration(TestMockConfiguration::class.java)
      .withPropertyValues(
        "axon.axonserver.enabled=false",
        "spring.application.name=my-test-application"
      ).run {

        Assertions.assertThat(it.getBean(CamundaEngineClientProperties::class.java)).isNotNull
        val props: CamundaEngineClientProperties = it.getBean(CamundaEngineClientProperties::class.java)

        Assertions.assertThat(props.applicationName).isEqualTo("my-test-application")
      }
  }

  @Test
  fun testAllChanged() {
    contextRunner
      .withUserConfiguration(TestMockConfiguration::class.java)
      .withUserConfiguration(AdditionalMockConfiguration::class.java)
      .withPropertyValues(
        "axon.axonserver.enabled=false",
        "spring.application.name=my-test-application",
        "polyflow.integration.client.camunda.applicationName=another-than-spring",
      ).run {

        Assertions.assertThat(it.getBean(CamundaEngineClientProperties::class.java)).isNotNull
        val props: CamundaEngineClientProperties = it.getBean(CamundaEngineClientProperties::class.java)

        Assertions.assertThat(props.applicationName).isEqualTo("another-than-spring")
      }
  }

  /**
   * Config class without configuration annotation not to confuse others.
   */
  private class AdditionalMockConfiguration {

  }

  /**
   * Config class without configuration annotation not to confuse others.
   */
  private class TestMockConfiguration {

    @Bean
    fun eventSerializer(): Serializer = XStreamSerializer.builder().build()

    @Bean
    fun eventBus(): EventBus = mock()

    @Bean
    fun commandGateway(): CommandGateway = mock()

    @Bean
    fun runtimeService(): RuntimeService = mock()

    @Bean
    fun taskService(): TaskService = mock()

  }

}
