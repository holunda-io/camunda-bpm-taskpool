package io.holunda.polyflow.example.tasklist.rest

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

object Rest {
  const val REQUEST_PATH = "/tasklist/rest"
  const val REACTIVE_PATH = "/tasklist/reactive"
}

@ResponseStatus(HttpStatus.NOT_FOUND)
class ElementNotFoundException : RuntimeException()
