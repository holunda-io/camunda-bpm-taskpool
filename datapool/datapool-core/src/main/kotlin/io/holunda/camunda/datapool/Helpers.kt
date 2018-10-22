package io.holunda.camunda.datapool

import java.util.*

fun <T> Optional<T>.ifPresentOrElse(presentConsumer: (T) -> Unit, missingCallback: () -> Unit) {
  if (this.isPresent) {
    presentConsumer(this.get())
  } else {
    missingCallback()
  }
}
