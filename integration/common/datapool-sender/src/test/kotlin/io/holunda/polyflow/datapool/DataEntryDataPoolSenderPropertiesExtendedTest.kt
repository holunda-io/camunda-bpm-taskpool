package io.holunda.polyflow.datapool

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.axonframework.commandhandling.gateway.CommandGateway
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.context.annotation.UserConfigurations
import org.springframework.boot.test.context.runner.ApplicationContextRunner
import org.springframework.context.annotation.Bean

class DataEntryDataPoolSenderPropertiesExtendedTest {

  private val contextRunner = ApplicationContextRunner()
    .withConfiguration(UserConfigurations.of(DataEntrySenderConfiguration::class.java))

  @Test
  fun `test minimal`() {
    contextRunner
      .withUserConfiguration(TestMockConfiguration::class.java)
      .withPropertyValues(
        "spring.application.name=my-test-application"
      ).run {

        assertThat(it.getBean(DataEntrySenderProperties::class.java)).isNotNull
        val props: DataEntrySenderProperties = it.getBean(DataEntrySenderProperties::class.java)

        assertThat(props.applicationName).isEqualTo("my-test-application")
        assertThat(props.enabled).isFalse
        assertThat(props.type).isEqualTo(DataEntrySenderType.simple)
        assertThat(props.sendWithinTransaction).isFalse()
      }
  }

  @Test
  fun `test all changed`() {
    contextRunner
      .withUserConfiguration(TestMockConfiguration::class.java)
      .withPropertyValues(
        "spring.application.name=my-test-application",
        "polyflow.integration.sender.data-entry.application-name=another-than-spring",
        "polyflow.integration.sender.data-entry.enabled=true",
        "polyflow.integration.sender.data-entry.type=tx",
        "polyflow.integration.sender.data-entry.send-within-transaction=true",
      ).run {

        assertThat(it.getBean(DataEntrySenderProperties::class.java)).isNotNull
        val props: DataEntrySenderProperties = it.getBean(DataEntrySenderProperties::class.java)

        assertThat(props.applicationName).isEqualTo("another-than-spring")

        assertThat(props.enabled).isTrue
        assertThat(props.type).isEqualTo(DataEntrySenderType.tx)
        assertThat(props.sendWithinTransaction).isTrue
      }
  }


  class TestMockConfiguration {

    @Bean
    fun commandGateway(): CommandGateway = mock()

    @Bean
    @Qualifier("payloadObjectMapper")
    fun objectMapper(): ObjectMapper = jacksonObjectMapper()
  }
}
