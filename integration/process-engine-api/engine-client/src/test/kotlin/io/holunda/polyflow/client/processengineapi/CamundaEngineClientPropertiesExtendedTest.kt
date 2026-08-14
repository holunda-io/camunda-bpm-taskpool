package io.holunda.polyflow.client.processengineapi

import com.thoughtworks.xstream.XStream
import com.thoughtworks.xstream.security.AnyTypePermission
import dev.bpmcrafters.processengineapi.process.StartProcessApi
import dev.bpmcrafters.processengineapi.task.UserTaskCompletionApi
import dev.bpmcrafters.processengineapi.task.UserTaskModificationApi
import dev.bpmcrafters.processengineapi.task.support.UserTaskSupport
import org.assertj.core.api.Assertions
import org.axonframework.commandhandling.CommandBus
import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.eventhandling.EventBus
import org.axonframework.queryhandling.QueryBus
import org.axonframework.serialization.Serializer
import org.axonframework.serialization.xml.XStreamSerializer
import org.axonframework.springboot.autoconfig.MetricsAutoConfiguration
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.springframework.boot.autoconfigure.AutoConfigurations
import org.springframework.boot.autoconfigure.AutoConfigureBefore
import org.springframework.boot.test.context.runner.ApplicationContextRunner
import org.springframework.context.annotation.Bean

class CamundaEngineClientPropertiesExtendedTest {

  private val contextRunner = ApplicationContextRunner()
    .withConfiguration(AutoConfigurations.of(TestMockConfiguration::class.java))
    .withUserConfiguration(
      EngineClientAutoConfiguration::class.java
    )

  @Test
  fun testMinimal() {
    contextRunner
      .withPropertyValues(
        "camunda.bpm.enabled=false",
        "axon.axonserver.enabled=false",
        "spring.application.name=my-test-application"
      ).run {

        Assertions.assertThat(it.getBean(EngineClientProperties::class.java)).isNotNull
        val props: EngineClientProperties = it.getBean(EngineClientProperties::class.java)

        Assertions.assertThat(props.applicationName).isEqualTo("my-test-application")
      }
  }

  @Test
  fun testAllChanged() {
    contextRunner
      .withPropertyValues(
        "camunda.bpm.enabled=false",
        "axon.axonserver.enabled=false",
        "spring.application.name=my-test-application",
        "polyflow.integration.client.processengineapi.application-name=another-than-spring",
      ).run {

        Assertions.assertThat(it.getBean(EngineClientProperties::class.java)).isNotNull
        val props: EngineClientProperties = it.getBean(EngineClientProperties::class.java)

        Assertions.assertThat(props.applicationName).isEqualTo("another-than-spring")
      }
  }

  /**
   * Config class without configuration annotation not to confuse others.
   */
  @AutoConfigureBefore(MetricsAutoConfiguration::class)
  class TestMockConfiguration {

    @Bean
    fun eventSerializer(): Serializer = XStreamSerializer.builder().xStream(XStream().apply { addPermission(AnyTypePermission.ANY) }).build()

    @Bean
    fun eventBus(): EventBus = mock()

    @Bean
    fun commandBus(): CommandBus = mock()

    @Bean
    fun commandGateway(): CommandGateway = mock()

    @Bean
    fun queryBus(): QueryBus = mock()

    @Bean
    fun startProcessApi(): StartProcessApi = mock()

    @Bean
    fun userTaskSupport(): UserTaskSupport = mock()

    @Bean
    fun userTaskCompletionApi(): UserTaskCompletionApi = mock()

    @Bean
    fun userTaskModificationApi(): UserTaskModificationApi = mock()

  }

}
