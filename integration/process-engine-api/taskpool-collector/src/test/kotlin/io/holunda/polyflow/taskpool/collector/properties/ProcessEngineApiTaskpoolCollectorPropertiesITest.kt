package io.holunda.polyflow.taskpool.collector.properties

import io.holunda.polyflow.spring.ApplicationNameBeanPostProcessor
import io.holunda.polyflow.taskpool.collector.ProcessEngineApiTaskpoolCollectorProperties
import io.holunda.polyflow.taskpool.sender.SenderProperties
import io.holunda.polyflow.taskpool.sender.gateway.CommandListGateway
import org.mockito.kotlin.mock
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Primary
import org.springframework.test.context.ActiveProfiles

@SpringBootTest(
  classes = [ProcessEngineApiTaskpoolCollectorPropertiesITest.PropertiesTestApplication::class],
  webEnvironment = SpringBootTest.WebEnvironment.MOCK
)
@ActiveProfiles("properties-itest")
internal class ProcessEngineApiTaskpoolCollectorPropertiesITest {

  @Autowired
  private lateinit var props: ProcessEngineApiTaskpoolCollectorProperties

//  FIXME: This fails
//  @Test
//  fun `should use defaults without properties in yaml`() {
//    assertThat(props.applicationName).isEqualTo("Foo")
//    assertThat(props.task.enabled).isTrue
//    assertThat(props.task.enricher.type).isEqualTo(TaskCollectorEnricherType.processVariables)
//  }

  @SpringBootApplication
  @EnableConfigurationProperties(ProcessEngineApiTaskpoolCollectorProperties::class, SenderProperties::class)
  @Import(ApplicationNameBeanPostProcessor::class)
  class PropertiesTestApplication {
    /**
     * Gateway.
     */
    @Bean
    @Primary
    fun testCommandListGateway(): CommandListGateway = mock()
  }


}

