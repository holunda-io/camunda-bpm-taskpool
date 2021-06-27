package io.holunda.camunda.taskpool.example.tasklist.rest

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

/**
 * REST constants.
 */
object Rest {
  const val REQUEST_PATH = "/polyflow-platform/rest"
}

/**
 * Exception thrown if element does not exist.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
class ElementNotFoundException : RuntimeException()
