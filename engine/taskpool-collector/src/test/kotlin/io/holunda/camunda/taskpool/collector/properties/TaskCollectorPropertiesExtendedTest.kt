package io.holunda.camunda.taskpool.collector.properties

import io.holunda.camunda.taskpool.TaskCollectorConfiguration
import io.holunda.camunda.taskpool.TaskCollectorEnricherType
import io.holunda.camunda.taskpool.TaskCollectorProperties
import io.holunda.camunda.taskpool.TaskSenderType
import io.holunda.camunda.taskpool.enricher.VariablesEnricher
import io.holunda.camunda.taskpool.sender.EngineTaskCommandSender
import junit.framework.Assert.assertNotNull
import junit.framework.Assert.fail
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.eventhandling.EventBus
import org.axonframework.serialization.Serializer
import org.axonframework.serialization.xml.XStreamSerializer
import org.junit.Test
import org.mockito.Mockito.mock
import org.springframework.boot.context.annotation.UserConfigurations
import org.springframework.boot.test.context.runner.ApplicationContextRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

class TaskCollectorPropertiesExtendedTest {

  private val contextRunner = ApplicationContextRunner()
    .withConfiguration(UserConfigurations.of(TaskCollectorConfiguration::class.java))

  @Test
  fun testMinimal() {
    contextRunner
      .withUserConfiguration(TestMockConfiguration::class.java)
      .withPropertyValues(
        "spring.application.name=my-test-application",
        "camunda.taskpool.collector.tasklist-url=http://some"
      ).run {

        assertNotNull(it.getBean(TaskCollectorProperties::class.java))
        val props: TaskCollectorProperties = it.getBean(TaskCollectorProperties::class.java)

        // deprecated should not be used.
        assertThat(props.enricher).isNull()

        assertThat(props.sendCommandsEnabled).isFalse
        assertThat(props.applicationName).isEqualTo("my-test-application")

        assertThat(props.processDefinition.enabled).isFalse

        assertThat(props.processInstance.enabled).isFalse

        assertThat(props.task.enabled).isTrue
        assertThat(props.task.sender.type).isEqualTo(TaskSenderType.tx)
        assertThat(props.task.sender.sendWithinTransaction).isFalse

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
        "camunda.taskpool.collector.tasklist-url=http://some",
        "camunda.taskpool.collector.applicationName=another-than-spring",
        "camunda.taskpool.collector.send-commands-enabled=true",
        "camunda.taskpool.collector.process-definition.enabled=true",
        "camunda.taskpool.collector.process-instance.enabled=true",
        "camunda.taskpool.collector.task.sender.enabled=false",
        "camunda.taskpool.collector.task.sender.type=custom",
        "camunda.taskpool.collector.task.sender.send-within-transaction=true",
        "camunda.taskpool.collector.task.enricher.type=custom",
      ).run {

        assertNotNull(it.getBean(TaskCollectorProperties::class.java))
        val props: TaskCollectorProperties = it.getBean(TaskCollectorProperties::class.java)

        // deprecated should not be used.
        assertThat(props.enricher).isNull()

        assertThat(props.applicationName).isEqualTo("another-than-spring")
        assertThat(props.sendCommandsEnabled).isTrue

        assertThat(props.processDefinition.enabled).isTrue

        assertThat(props.processInstance.enabled).isTrue

        assertThat(props.task.sender.type).isEqualTo(TaskSenderType.custom)
        assertThat(props.task.sender.sendWithinTransaction).isTrue

        assertThat(props.task.enricher.type).isEqualTo(TaskCollectorEnricherType.custom)
      }
  }


  @Test
  fun testDeprecation() {
    // spring context exception is always an IllegalStateException
    assertThatExceptionOfType(IllegalStateException::class.java).isThrownBy {
      contextRunner
        .withUserConfiguration(TestMockConfiguration::class.java)
        .withPropertyValues(
          "spring.application.name=my-test-application",
          "camunda.taskpool.collector.tasklist-url=http://some",
          "camunda.taskpool.collector.enricher.type=no" // deprecated property.
        ).run {
          assertNotNull(it.getBean(TaskCollectorProperties::class.java))
          fail("Spring context is initialized, but it should not since the config is invalid.")
        }
    }
  }
  /**
   * Config class without configuration annotation not to confuse others.
   */
  private class AdditionalMockConfiguration {
    @Bean
    fun engineTaskCommandSender(): EngineTaskCommandSender = mock(EngineTaskCommandSender::class.java)

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
