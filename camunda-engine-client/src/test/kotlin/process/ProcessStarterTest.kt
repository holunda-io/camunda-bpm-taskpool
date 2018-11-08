package io.holunda.camunda.client.process

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.verifyNoMoreInteractions
import com.nhaarman.mockito_kotlin.whenever
import org.assertj.core.api.Assertions.assertThat
import org.camunda.bpm.engine.RuntimeService
import org.camunda.bpm.engine.variable.Variables
import org.camunda.bpm.engine.variable.Variables.stringValue
import org.camunda.bpm.extension.mockito.process.ProcessInstanceFake
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoRule


class ProcessStarterTest {

  @get: Rule
  val mockitoRule: MockitoRule = MockitoJUnit.rule()

  @Mock
  private lateinit var runtimeService: RuntimeService

  private lateinit var processStarter: ProcessStarter

  @Before
  fun init() {
    processStarter = ProcessStarter(runtimeService)
  }


  @Test
  fun `should start process`() {

    val payload = Variables.createVariables().putValueTyped("my-user-input", stringValue("my value"))

    whenever(runtimeService.startProcessInstanceByKey(any(), any(), any<Map<String, Any>>()))
      .thenReturn(ProcessInstanceFake
        .builder()
        .id("4711")
        .processDefinitionId("myProcess")
        .processInstanceId("0815")
        .businessKey("business 123")
        .build())

    val instance = processStarter.startProcess("process 1", payload = payload, businessKey = "business 789")
    assertThat(instance).isEqualTo("0815")

    verify(runtimeService).startProcessInstanceByKey("process 1", "business 789", payload)
    verifyNoMoreInteractions(runtimeService)

  }

}


