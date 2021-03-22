package io.holunda.camunda.taskpool.collector.properties

import io.holunda.camunda.taskpool.CamundaTaskpoolCollectorConfiguration
import io.holunda.camunda.taskpool.CamundaTaskpoolCollectorProperties
import io.holunda.camunda.taskpool.TaskCollectorEnricherType
import io.holunda.camunda.taskpool.collector.task.VariablesEnricher
import org.assertj.core.api.Assertions.assertThat
import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.eventhandling.EventBus
import org.axonframework.serialization.Serializer
import org.axonframework.serialization.xml.XStreamSerializer
import org.junit.Test
import org.mockito.Mockito.mock
import org.springframework.boot.context.annotation.UserConfigurations
import org.springframework.boot.test.context.runner.ApplicationContextRunner
import org.springframework.context.annotation.Bean

class TaskCollectorPropertiesExtendedTest {

  private val contextRunner = ApplicationContextRunner()
    .withConfiguration(UserConfigurations.of(CamundaTaskpoolCollectorConfiguration::class.java))

  @Test
  fun testMinimal() {
    contextRunner
      .withUserConfiguration(TestMockConfiguration::class.java)
      .withPropertyValues(
        "spring.application.name=my-test-application"
      ).run {

        assertThat(it.getBean(CamundaTaskpoolCollectorProperties::class.java)).isNotNull
        val props: CamundaTaskpoolCollectorProperties = it.getBean(CamundaTaskpoolCollectorProperties::class.java)

        assertThat(props.applicationName).isEqualTo("my-test-application")
        assertThat(props.processDefinition.enabled).isFalse
        assertThat(props.processInstance.enabled).isFalse
        assertThat(props.task.enabled).isTrue
        assertThat(props.task.enricher.type).isEqualTo(TaskCollectorEnricherType.processVariables)
      }
  }

  @Test
  fun testAllChanged() {
    contextRunner
      .withUserConfiguration(TestMockConfiguration::class.java)
      .withUserConfiguration(AdditionalMockConfiguration::class.java)
      .withPropertyValues(
        "spring.application.name=my-test-application",
        "polyflow.integration.collector.camunda.applicationName=another-than-spring",
        "polyflow.integration.collector.camunda.send-commands-enabled=true",
        "polyflow.integration.collector.camunda.process-definition.enabled=true",
        "polyflow.integration.collector.camunda.process-instance.enabled=true",
        "polyflow.integration.collector.camunda.task.enabled=true",
        "polyflow.integration.collector.camunda.task.enricher.type=custom",
      ).run {

        assertThat(it.getBean(CamundaTaskpoolCollectorProperties::class.java)).isNotNull
        val props: CamundaTaskpoolCollectorProperties = it.getBean(CamundaTaskpoolCollectorProperties::class.java)

        assertThat(props.applicationName).isEqualTo("another-than-spring")

        assertThat(props.processDefinition.enabled).isTrue
        assertThat(props.processInstance.enabled).isTrue
        assertThat(props.task.enabled).isTrue

        assertThat(props.task.enricher.type).isEqualTo(TaskCollectorEnricherType.custom)
      }
  }

  /**
   * Config class without configuration annotation not to confuse others.
   */
  private class AdditionalMockConfiguration {
    @Bean
    fun variablesEnricher(): VariablesEnricher = mock(VariablesEnricher::class.java)
  }

  /**
   * Config class without configuration annotation not to confuse others.
   */
  private class TestMockConfiguration {

    @Bean
    fun eventSerializer(): Serializer = XStreamSerializer.builder().build()

    @Bean
    fun eventBus(): EventBus = mock(EventBus::class.java)

    @Bean
    fun commandGateway(): CommandGateway = mock(CommandGateway::class.java)
  }
}
