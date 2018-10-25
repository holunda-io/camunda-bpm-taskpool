package io.holunda.camunda.taskpool.example.tasklist

import io.holunda.camunda.taskpool.api.task.ProcessReference
import io.holunda.camunda.taskpool.view.Task
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class TaskUrlResolverPropertiesTest {

  private val props: TaskUrlResolverProperties = TaskUrlResolverProperties(
    default = "id/\${task.id}",
    tasks = mutableMapOf("foo" to "forms/\${task.formKey}/id/\${task.id}")
  )

  @Test
  fun `default when not configured`() {
    assertThat(props.getUrlTemplate("xxx")).isEqualTo("id/\${task.id}")
  }

  @Test
  fun `specific when configured`() {
    assertThat(props.getUrlTemplate("foo")).isEqualTo("forms/\${task.formKey}/id/\${task.id}")
  }

}
