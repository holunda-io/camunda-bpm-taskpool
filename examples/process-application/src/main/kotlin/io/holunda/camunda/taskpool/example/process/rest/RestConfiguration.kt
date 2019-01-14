package io.holunda.camunda.taskpool.example.process.rest

import org.springframework.context.annotation.Configuration

@Configuration
open class RestConfiguration {

  companion object {
    const val REST_PREFIX = "/example-process-approval/rest"
  }

}
