package io.holunda.polyflow.client.processengineapi

import dev.bpmcrafters.processengineapi.process.StartProcessApi
import dev.bpmcrafters.processengineapi.task.UserTaskCompletionApi
import dev.bpmcrafters.processengineapi.task.UserTaskModificationApi
import dev.bpmcrafters.processengineapi.task.support.UserTaskSupport
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.MOCK
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean

@SpringBootTest(classes = [ProcessEngineApiClientPropertiesTestApplication::class], webEnvironment = MOCK)
@ActiveProfiles("properties-itest")
class ProcessEngineApiClientPropertiesITest {

//  @MockitoBean
//  lateinit var runtimeService: RuntimeService
//
//  @MockitoBean
//  lateinit var taskService: TaskService

  @MockitoBean
  lateinit var startProcessApi: StartProcessApi

  @MockitoBean
  lateinit var taskQuery: UserTaskSupport

  @MockitoBean
  lateinit var taskCompletionService: UserTaskCompletionApi

  @MockitoBean
  lateinit var userTaskModificationApi: UserTaskModificationApi

  @Autowired
  lateinit var props: EngineClientProperties



  @Test
  fun test_properties() {
    assertThat(props.applicationName).isEqualTo("Foo")
  }
}
