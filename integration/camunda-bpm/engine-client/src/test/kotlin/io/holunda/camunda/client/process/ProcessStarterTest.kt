package io.holunda.polyflow.client.camunda.process

import org.assertj.core.api.Assertions.assertThat
import org.camunda.bpm.engine.RuntimeService
import org.camunda.bpm.engine.variable.Variables
import org.camunda.bpm.engine.variable.Variables.stringValue
import org.camunda.bpm.extension.mockito.process.ProcessInstanceFake
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever


@ExtendWith(MockitoExtension::class)
class ProcessStarterTest {

  @Mock
  private lateinit var runtimeService: RuntimeService

  private lateinit var processStarter: ProcessStarter

  @BeforeEach
  fun init() {
    processStarter = ProcessStarter(runtimeService)
  }


  @Test
  fun `should start process`() {

    val payload = Variables.createVariables().putValueTyped("my-user-input", stringValue("my value"))

    whenever(runtimeService.startProcessInstanceByKey(any(), any(), any<Map<String, Any>>()))
      .thenReturn(
        ProcessInstanceFake
          .builder()
          .id("4711")
          .processDefinitionId("myProcess")
          .processInstanceId("0815")
          .businessKey("business 123")
          .build()
      )

    val instance = processStarter.startProcess("process 1", payload = payload, businessKey = "business 789")
    assertThat(instance).isEqualTo("0815")

    verify(runtimeService).startProcessInstanceByKey("process 1", "business 789", payload)
    verifyNoMoreInteractions(runtimeService)

  }

}


