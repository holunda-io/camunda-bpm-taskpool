package io.holunda.polyflow.urlresolver

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.holunda.camunda.taskpool.api.task.ProcessReference
import io.holunda.polyflow.view.ProcessDefinition
import io.holunda.polyflow.view.Task
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test


class FormUrlResolverTest {

  private val task = Task(
    id = "1",
    taskDefinitionKey = "foo",
    formKey = "formKey",
    sourceReference = ProcessReference("", "", "", "", "", "test")
  )

  private val process = ProcessDefinition(
    processDefinitionId = "182734623846",
    processDefinitionKey = "foo",
    processDefinitionVersion = 1,
    processName = "myName",
    applicationName = "test",
    formKey = "startMe"
  )


  @Test
  fun resolveTaskTemplate() {

    val resolver = PropertyBasedFormUrlResolver(
      props = FormUrlResolverProperties(
        defaultTaskTemplate = "id/\${id}",
        defaultApplicationTemplate = "holunda://foo:1234/\${applicationName}",
        applications = mapOf("test" to FormUrlResolverProperties.Application(url = null, tasks = mapOf("foo" to "forms/\${formKey}/id/\${id}")))
      ), objectMapper = jacksonObjectMapper()
    )
    assertThat(resolver.resolveUrl(task)).isEqualTo("holunda://foo:1234/test/forms/formKey/id/1")
  }

  @Test
  fun resolveTaskAndApplicationTemplate() {

    val resolver = PropertyBasedFormUrlResolver(
      props = FormUrlResolverProperties(
        defaultApplicationTemplate = "holunda://foo:1234/\${applicationName}",
        applications = mapOf(
          "test" to FormUrlResolverProperties.Application(
            url = "muppetshow://kermithost:0987/my",
            tasks = mapOf("foo" to "views/\${formKey}/\${id}")
          )
        )
      ), objectMapper = jacksonObjectMapper()
    )

    assertThat(resolver.resolveUrl(task)).isEqualTo("muppetshow://kermithost:0987/my/views/formKey/1")
  }

  @Test
  fun resolveTaskUrlByTaskKeyAndApplication() {

    val resolver = PropertyBasedFormUrlResolver(
      props = FormUrlResolverProperties(
        defaultApplicationTemplate = "holunda://foo:1234/\${applicationName}",
        applications = mapOf(
          "test" to FormUrlResolverProperties.Application(tasks = mapOf("foo" to "views/\${formKey}/\${id}")),
          "test2" to FormUrlResolverProperties.Application(tasks = mapOf("foo" to "wrong/\${formKey}/\${id}"))
        )
      ), objectMapper = jacksonObjectMapper()
    )
    assertThat(resolver.resolveUrl(task)).isEqualTo("holunda://foo:1234/test/views/formKey/1")
  }

  @Test
  fun resolveProcessUrlByProcessKeyAndApplication() {

    val resolver = PropertyBasedFormUrlResolver(
      props = FormUrlResolverProperties(
        defaultApplicationTemplate = "holunda://foo:1234/\${applicationName}",
        applications = mapOf(
          "test" to FormUrlResolverProperties.Application(processes = mapOf("foo" to "start/\${processDefinitionKey}/\${formKey}"))
        )
      ), objectMapper = jacksonObjectMapper()
    )

    assertThat(resolver.resolveUrl(process)).isEqualTo("holunda://foo:1234/test/start/foo/startMe")
  }

}
