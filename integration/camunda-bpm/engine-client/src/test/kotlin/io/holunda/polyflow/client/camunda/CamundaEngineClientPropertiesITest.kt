package io.holunda.polyflow.client.camunda

import org.assertj.core.api.Assertions.assertThat
import org.camunda.bpm.engine.RuntimeService
import org.camunda.bpm.engine.TaskService
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.MOCK
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.ActiveProfiles

@SpringBootTest(classes = [CamundaEngineClientPropertiesTestApplication::class], webEnvironment = MOCK)
@ActiveProfiles("properties-itest")
class CamundaEngineClientPropertiesITest {

  @MockBean
  lateinit var runtimeService: RuntimeService

  @MockBean
  lateinit var taskService: TaskService

  @Autowired
  lateinit var props: CamundaEngineClientProperties

  @Test
  fun test_properties() {
    assertThat(props.applicationName).isEqualTo("Foo")
  }
}
