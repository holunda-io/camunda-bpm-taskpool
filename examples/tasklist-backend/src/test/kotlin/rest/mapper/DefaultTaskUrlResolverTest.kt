package io.holunda.camunda.taskpool.example.tasklist.rest.mapper

import io.holunda.camunda.taskpool.api.task.ProcessReference
import io.holunda.camunda.taskpool.example.tasklist.TaskUrlResolverProperties
import io.holunda.camunda.taskpool.view.Task
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test


class DefaultTaskUrlResolverTest {

  private val resolver = DefaultTaskUrlResolver(
    lookup = DefaultApplicationUrlLookup(),
    props = TaskUrlResolverProperties(
      default = "id/\${id}",
      tasks = mutableMapOf("foo" to "forms/\${formKey}/id/\${id}")
    )
  )

  @Test
  fun resolve() {

    val task = Task(
      id = "1",
      taskDefinitionKey = "foo",
      formKey = "formKey",
      sourceReference = ProcessReference("", "", "", "", "", "test")
    )

    assertThat(resolver.resolveUrl(task)).isEqualTo("http://localhost:8080/test/forms/formKey/id/1")
  }
}
