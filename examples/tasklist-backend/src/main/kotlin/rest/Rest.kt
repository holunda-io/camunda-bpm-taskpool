package io.holunda.camunda.taskpool.example.tasklist.rest

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

object Rest {
  const val REQUEST_PATH = "/tasklist/rest"
}

@ResponseStatus(HttpStatus.NOT_FOUND)
class ElementNotFoundException : RuntimeException()
