package io.holunda.polyflow.taskpool.collector.properties

import io.holunda.polyflow.taskpool.collector.CamundaTaskpoolCollectorProperties
import io.holunda.polyflow.taskpool.collector.TaskCollectorEnricherType
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.PropertySource
import org.springframework.test.context.ActiveProfiles

@SpringBootTest(classes = [CamundaTaskpoolCollectorPropertiesITest.PropertiesTestApplication::class], webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ActiveProfiles("properties-itest")
internal class CamundaTaskpoolCollectorPropertiesITest {

  @Autowired
  private lateinit var props: CamundaTaskpoolCollectorProperties

  @Test
  fun `should use defaults without properties in yaml`() {
    assertThat(props.applicationName).isEqualTo("Foo")
    assertThat(props.task.enabled).isTrue
    assertThat(props.task.enricher.type).isEqualTo(TaskCollectorEnricherType.processVariables)
    assertThat(props.processInstance.enabled).isTrue
    assertThat(props.processVariable.enabled).isTrue
    assertThat(props.processDefinition.enabled).isFalse
  }

  @SpringBootApplication
  @EnableConfigurationProperties(CamundaTaskpoolCollectorProperties::class)
  class PropertiesTestApplication

}

