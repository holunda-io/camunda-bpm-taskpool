package io.holunda.polyflow.client.camunda.process

import dev.bpmcrafters.processengineapi.CommonRestrictions
import dev.bpmcrafters.processengineapi.process.ProcessInformation
import dev.bpmcrafters.processengineapi.process.StartProcessApi
import dev.bpmcrafters.processengineapi.process.StartProcessByDefinitionCmd
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever
import java.util.concurrent.CompletableFuture


@ExtendWith(MockitoExtension::class)
class ProcessStarterTest {

  @Mock
  private lateinit var startProcessApi: StartProcessApi

  private lateinit var processStarter: ProcessStarter

  @BeforeEach
  fun init() {
    processStarter = ProcessStarter(startProcessApi)
  }


  @Test
  fun `should start process`() {
    val payload = mapOf(
      "var1" to "value1" as Object,
    )

    whenever(startProcessApi.startProcess(any())).thenReturn(
      CompletableFuture.completedFuture(
        ProcessInformation(
          instanceId = "0815",
          meta = mapOf()
        )
      )
    )

    val instance = processStarter.startProcess("process 1", payload = payload, businessKey = "business 789")
    assertThat(instance).isEqualTo("0815")

    val argumentCaptor = argumentCaptor<StartProcessByDefinitionCmd>()
    verify(startProcessApi).startProcess(argumentCaptor.capture())
    verifyNoMoreInteractions(startProcessApi)
    val startProcessByDefinitionCmd = argumentCaptor.firstValue
    assertThat(startProcessByDefinitionCmd.definitionKey).isEqualTo("process 1")
    assertThat(startProcessByDefinitionCmd.payloadSupplier.get()).isEqualTo(payload)
    assertThat(startProcessByDefinitionCmd.restrictions).containsEntry(CommonRestrictions.BUSINESS_KEY, "business 789")

  }

}


