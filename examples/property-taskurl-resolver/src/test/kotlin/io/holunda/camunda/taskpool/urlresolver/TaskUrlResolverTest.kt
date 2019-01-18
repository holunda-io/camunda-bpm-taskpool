package io.holunda.camunda.taskpool.urlresolver

import io.holunda.camunda.taskpool.api.task.ProcessReference
import io.holunda.camunda.taskpool.view.Task
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test


class TaskUrlResolverTest {

  private val task = Task(
    id = "1",
    taskDefinitionKey = "foo",
    formKey = "formKey",
    sourceReference = ProcessReference("", "", "", "", "", "test")
  )


  @Test
  fun resolveTaskTemplate() {

    val resolver = PropertyBasedTaskUrlResolver(
      props = TaskUrlResolverProperties(
        defaultTaskTemplate = "id/\${id}",
        defaultApplicationTemplate = "holunda://foo:1234/\${applicationName}",
        applications = mapOf("test" to TaskUrlResolverProperties.Application(url = null, tasks = mapOf("foo" to "forms/\${formKey}/id/\${id}")))
      )
    )
    assertThat(resolver.resolveUrl(task)).isEqualTo("holunda://foo:1234/test/forms/formKey/id/1")
  }

  @Test
  fun resolveTaskAndApplicationTemplate() {

    val resolver = PropertyBasedTaskUrlResolver(
      props = TaskUrlResolverProperties(
        defaultApplicationTemplate = "holunda://foo:1234/\${applicationName}",
        applications = mapOf("test" to TaskUrlResolverProperties.Application(url = "muppetshow://kermithost:0987/my", tasks = mapOf("foo" to "views/\${formKey}/\${id}")))
      )
    )

    assertThat(resolver.resolveUrl(task)).isEqualTo("muppetshow://kermithost:0987/my/views/formKey/1")
  }

  @Test
  fun resolveTaskUrlByTaskKeyAndApplication() {

    val resolver = PropertyBasedTaskUrlResolver(
      props = TaskUrlResolverProperties(
        defaultApplicationTemplate = "holunda://foo:1234/\${applicationName}",
        applications = mapOf(
          "test" to TaskUrlResolverProperties.Application(tasks = mapOf("foo" to "views/\${formKey}/\${id}")),
          "test2" to TaskUrlResolverProperties.Application(tasks = mapOf("foo" to "wrong/\${formKey}/\${id}"))
        )
      )
    )
    assertThat(resolver.resolveUrl(task)).isEqualTo("holunda://foo:1234/test/views/formKey/1")
  }

}
