package io.holunda.polyflow.example.process.approval.rest

import io.holunda.polyflow.example.process.approval.rest.api.EnvironmentApi
import io.holunda.polyflow.example.process.approval.rest.model.EnvironmentDto
import io.holunda.polyflow.taskpool.collector.CamundaTaskpoolCollectorProperties
import io.holunda.polyflow.urlresolver.TasklistUrlResolver
import io.swagger.annotations.Api
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.ok
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping

@Api(tags = ["Environment"])
@Controller
@RequestMapping(path = [Rest.REST_PREFIX])
class EnvironmentController(
  private val properties: CamundaTaskpoolCollectorProperties,
  private val tasklistUrlResolver: TasklistUrlResolver
) : EnvironmentApi {

  override fun getEnvironment(): ResponseEntity<EnvironmentDto> =
    ok(
      EnvironmentDto()
        .applicationName(properties.applicationName)
        .tasklistUrl(tasklistUrlResolver.getTasklistUrl())
    )
}
