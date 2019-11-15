package io.holunda.camunda.taskpool.example.process.rest

import io.holunda.camunda.taskpool.TaskCollectorProperties
import io.holunda.camunda.taskpool.example.process.rest.api.EnvironmentApi
import io.holunda.camunda.taskpool.example.process.rest.model.EnvironmentDto
import io.holunda.camunda.taskpool.urlresolver.TasklistUrlResolver
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.ok
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping(path = [Rest.REST_PREFIX])
class EnvironmentController(
  private val properties: TaskCollectorProperties,
  private val tasklistUrlResolver: TasklistUrlResolver
) : EnvironmentApi {

  override fun getEnvironment(): ResponseEntity<EnvironmentDto> =
    ok(
      EnvironmentDto()
        .applicationName(properties.enricher.applicationName)
        .tasklistUrl(tasklistUrlResolver.getTasklistUrl())
    )
}
