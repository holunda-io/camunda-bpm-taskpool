package io.holunda.polyflow.taskpool.collector.process.definition

import org.assertj.core.api.Assertions.assertThat
import org.camunda.bpm.engine.impl.context.Context
import org.camunda.bpm.engine.impl.interceptor.Command
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import org.springframework.context.ApplicationEventPublisher

class ProcessDefinitionLifecycleCommandInterceptorTest {

  @Test
  fun `delegates commands that run without a Camunda command context`() {
    check(Context.getCommandContext() == null)
    val eventPublisher = mock<ApplicationEventPublisher>()
    val command = mock<Command<String>>()
    val next = mock<CommandExecutor>()
    val interceptor = ProcessDefinitionLifecycleCommandInterceptor(eventPublisher).apply { setNext(next) }
    whenever(next.execute(command)).thenReturn("result")

    assertThat(interceptor.execute(command)).isEqualTo("result")

    verify(next).execute(command)
    verifyNoInteractions(eventPublisher)
  }

  @Test
  fun `delegates null command results without a Camunda command context`() {
    check(Context.getCommandContext() == null)
    val eventPublisher = mock<ApplicationEventPublisher>()
    val command = mock<Command<String?>>()
    val next = mock<CommandExecutor>()
    val interceptor = ProcessDefinitionLifecycleCommandInterceptor(eventPublisher).apply { setNext(next) }
    whenever(next.execute(command)).thenReturn(null)

    assertThat(interceptor.execute(command)).isNull()

    verify(next).execute(command)
    verifyNoInteractions(eventPublisher)
  }
}
