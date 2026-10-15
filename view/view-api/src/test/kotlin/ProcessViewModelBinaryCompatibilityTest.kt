package io.holunda.polyflow.view

import io.holunda.camunda.taskpool.api.task.SourceReference
import org.assertj.core.api.Assertions.assertThatCode
import org.junit.jupiter.api.Test

internal class ProcessViewModelBinaryCompatibilityTest {

  @Test
  fun `retains the process definition and instance constructors used by existing Kotlin consumers`() {
    val marker = Class.forName("kotlin.jvm.internal.DefaultConstructorMarker")

    assertThatCode {
      ProcessDefinition::class.java.getConstructor(
        String::class.java,
        String::class.java,
        Int::class.javaPrimitiveType,
        String::class.java,
        String::class.java,
        String::class.java,
        String::class.java,
        String::class.java,
        Boolean::class.javaPrimitiveType,
        Set::class.java,
        Set::class.java,
        Int::class.javaPrimitiveType,
        marker,
      )
      ProcessInstance::class.java.getConstructor(
        String::class.java,
        SourceReference::class.java,
        ProcessInstanceState::class.java,
        String::class.java,
        String::class.java,
        String::class.java,
        String::class.java,
        String::class.java,
        String::class.java,
        Int::class.javaPrimitiveType,
        marker,
      )
    }.doesNotThrowAnyException()
  }
}
