package io.holunda.polyflow.taskpool.collector.properties

import com.thoughtworks.xstream.XStream
import io.holunda.polyflow.taskpool.collector.CamundaTaskpoolCollectorConfiguration
import io.holunda.polyflow.taskpool.collector.CamundaTaskpoolCollectorProperties
import io.holunda.polyflow.taskpool.collector.TaskCollectorEnricherType
import io.holunda.polyflow.taskpool.collector.task.TaskVariableLoader
import io.holunda.polyflow.taskpool.collector.task.VariablesEnricher
import io.holunda.polyflow.taskpool.sender.process.definition.ProcessDefinitionCommandSender
import io.holunda.polyflow.taskpool.sender.process.instance.ProcessInstanceCommandSender
import io.holunda.polyflow.taskpool.sender.process.variable.ProcessVariableCommandSender
import io.holunda.polyflow.taskpool.sender.task.EngineTaskCommandSender
import org.assertj.core.api.Assertions.assertThat
import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.eventhandling.EventBus
import org.axonframework.serialization.Serializer
import org.axonframework.serialization.xml.XStreamSerializer
import org.camunda.bpm.engine.RepositoryService
import org.camunda.bpm.engine.RuntimeService
import org.camunda.bpm.engine.TaskService
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor
import org.camunda.bpm.spring.boot.starter.property.CamundaBpmProperties
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.mock
import org.springframework.boot.context.annotation.UserConfigurations
import org.springframework.boot.test.context.runner.ApplicationContextRunner
import org.springframework.context.annotation.Bean

internal class CamundaTaskpoolCollectorPropertiesExtendedTest {

  private val contextRunner = ApplicationContextRunner()
    .withConfiguration(UserConfigurations.of(CamundaTaskpoolCollectorConfiguration::class.java))

  @Test
  fun `loads minimal configuration without properties set`() {
    contextRunner
      .withUserConfiguration(TestMockConfiguration::class.java)
      .withPropertyValues(
        "spring.application.name=my-test-application",
        "camunda.bpm.eventing.task=false"
      ).run {

        assertThat(it.getBean(CamundaTaskpoolCollectorProperties::class.java)).isNotNull
        val props: CamundaTaskpoolCollectorProperties = it.getBean(CamundaTaskpoolCollectorProperties::class.java)

        assertThat(props.applicationName).isEqualTo("my-test-application")
        assertThat(props.processDefinition.enabled).isFalse
        assertThat(props.processInstance.enabled).isTrue
        assertThat(props.processVariable.enabled).isTrue
        assertThat(props.task.enabled).isTrue
        assertThat(props.task.enricher.type).isEqualTo(TaskCollectorEnricherType.processVariables)
      }
  }

  @Test
  fun `loads all changed properties configuration`() {
    contextRunner
      .withUserConfiguration(TestMockConfiguration::class.java)
      .withUserConfiguration(AdditionalMockConfiguration::class.java)
      .withPropertyValues(
        "spring.application.name=my-test-application",
        "camunda.bpm.eventing.task=false",
        "polyflow.integration.collector.camunda.applicationName=another-than-spring",
        "polyflow.integration.collector.camunda.process-definition.enabled=true",
        "polyflow.integration.collector.camunda.process-instance.enabled=false",
        "polyflow.integration.collector.camunda.process-variable.enabled=false",
        "polyflow.integration.collector.camunda.task.enabled=true",
        "polyflow.integration.collector.camunda.task.enricher.type=custom",
      ).run {

        assertThat(it.getBean(CamundaTaskpoolCollectorProperties::class.java)).isNotNull
        val props: CamundaTaskpoolCollectorProperties = it.getBean(CamundaTaskpoolCollectorProperties::class.java)

        assertThat(props.applicationName).isEqualTo("another-than-spring")

        assertThat(props.processDefinition.enabled).isTrue
        assertThat(props.processInstance.enabled).isFalse
        assertThat(props.processVariable.enabled).isFalse
        assertThat(props.task.enabled).isTrue

        assertThat(props.task.enricher.type).isEqualTo(TaskCollectorEnricherType.custom)
      }
  }

  @Test
  fun `loads properties configuration to ignore listeners`() {
    contextRunner
      .withUserConfiguration(TestMockConfiguration::class.java)
      .withUserConfiguration(AdditionalMockConfiguration::class.java)
      .withPropertyValues(
        "spring.application.name=my-test-application",
        "camunda.bpm.eventing.task=false",
        "polyflow.integration.collector.camunda.task.enabled=true",
        "polyflow.integration.collector.camunda.task.enricher.type=custom",
        "polyflow.integration.collector.camunda.task.excluded-task-event-names=assignment",
        "polyflow.integration.collector.camunda.task.excluded-history-event-names=add-identity-link,delete-identity-link",
      ).run {

        assertThat(it.getBean(CamundaTaskpoolCollectorProperties::class.java)).isNotNull
        val props: CamundaTaskpoolCollectorProperties = it.getBean(CamundaTaskpoolCollectorProperties::class.java)

        assertThat(props.task.enabled).isTrue
        assertThat(props.task.collectTaskEvent("assignment")).isFalse()
        assertThat(props.task.collectHistoryEvent("add-identity-link")).isFalse()
        assertThat(props.task.collectHistoryEvent("delete-identity-link")).isFalse()
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
    fun eventSerializer(): Serializer = XStreamSerializer.builder().xStream(XStream()).build()

    @Bean
    fun eventBus(): EventBus = mock()

    @Bean
    fun commandGateway(): CommandGateway = mock()

    @Bean
    fun processDefinitionCommandSender(): ProcessDefinitionCommandSender = mock()

    @Bean
    fun processInstanceCommandSender(): ProcessInstanceCommandSender = mock()

    @Bean
    fun processVariableCommandSender(): ProcessVariableCommandSender = mock()

    @Bean
    fun engineTaskCommandSender(): EngineTaskCommandSender = mock()

    @Bean
    fun camundaBpmProperties(): CamundaBpmProperties = CamundaBpmProperties()

    @Bean
    fun repositoryService(): RepositoryService = mock()

    @Bean
    fun runtimeService(): RuntimeService = mock()

    @Bean
    fun taskService(): TaskService = mock()

    @Bean
    fun commandExecutor(): CommandExecutor = mock()
  }
}
