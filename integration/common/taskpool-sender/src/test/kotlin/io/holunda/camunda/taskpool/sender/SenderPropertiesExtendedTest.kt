package io.holunda.camunda.taskpool.sender

import io.holunda.camunda.taskpool.SenderConfiguration
import io.holunda.camunda.taskpool.SenderProperties
import io.holunda.camunda.taskpool.SenderType
import io.holunda.camunda.taskpool.sender.task.EngineTaskCommandSender
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

class SenderPropertiesExtendedTest {

  private val contextRunner = ApplicationContextRunner()
    .withConfiguration(UserConfigurations.of(SenderConfiguration::class.java))

  @Test
  fun testMinimal() {
    contextRunner
      .withUserConfiguration(TestMockConfiguration::class.java)
      .run {

        assertThat(it.getBean(SenderProperties::class.java)).isNotNull
        val props: SenderProperties = it.getBean(SenderProperties::class.java)

        assertThat(props.enabled).isFalse

        assertThat(props.processDefinition.enabled).isFalse
        assertThat(props.processInstance.enabled).isFalse
        assertThat(props.task.enabled).isFalse

        assertThat(props.task.type).isEqualTo(SenderType.tx)
        assertThat(props.task.sendWithinTransaction).isFalse

      }
  }

  @Test
  fun testAllChanged() {
    contextRunner
      .withUserConfiguration(TestMockConfiguration::class.java)
      .withUserConfiguration(AdditionalMockConfiguration::class.java)
      .withPropertyValues(
        "polyflow.integration.sender.enabled=true",
        "polyflow.integration.sender.process-definition.enabled=true",
        "polyflow.integration.sender.process-instance.enabled=true",
        "polyflow.integration.sender.task.enabled=true",
        "polyflow.integration.sender.task.type=custom",
        "polyflow.integration.sender.task.send-within-transaction=true"
      ).run {

        assertThat(it.getBean(SenderProperties::class.java)).isNotNull
        val props: SenderProperties = it.getBean(SenderProperties::class.java)

        assertThat(props.enabled).isTrue

        assertThat(props.processDefinition.enabled).isTrue
        assertThat(props.processInstance.enabled).isTrue
        assertThat(props.task.enabled).isTrue

        assertThat(props.task.type).isEqualTo(SenderType.custom)
        assertThat(props.task.sendWithinTransaction).isTrue

      }
  }


  /**
   * Config class without configuration annotation not to confuse others.
   */
  private class AdditionalMockConfiguration {
    @Bean
    fun engineTaskCommandSender(): EngineTaskCommandSender = mock(EngineTaskCommandSender::class.java)
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
