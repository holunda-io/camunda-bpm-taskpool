package io.holunda.polyflow.example.tasklist.rest

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

/**
 * Exception thrown if element is not allowed to be accessed.
 */
@ResponseStatus(HttpStatus.FORBIDDEN)
class NotAllowedException: RuntimeException()

