package io.holunda.camunda.client

import com.nhaarman.mockitokotlin2.mock
import org.assertj.core.api.Assertions.assertThat
import org.camunda.bpm.engine.RuntimeService
import org.camunda.bpm.engine.TaskService
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.MOCK
import org.springframework.context.annotation.Bean
import org.springframework.test.context.ActiveProfiles

@SpringBootTest(classes = [CamundaEngineClientPropertiesTestApplication::class, CamundaMockConfiguration::class], webEnvironment = MOCK)
@ActiveProfiles("properties-itest")
class CamundaEngineClientPropertiesITest {

  @Autowired
  lateinit var props: CamundaEngineClientProperties

  @Test
  fun test_properties() {
    assertThat(props.applicationName).isEqualTo("Foo")
  }
}

class CamundaMockConfiguration {
  @Bean
  fun runtimeService(): RuntimeService = mock()

  @Bean
  fun taskService(): TaskService = mock()
}

