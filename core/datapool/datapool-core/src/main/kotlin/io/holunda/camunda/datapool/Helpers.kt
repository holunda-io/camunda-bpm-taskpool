package io.holunda.camunda.datapool

import java.util.*

/**
 * Allows to call a consumer if the element is present or call another callback, if the element is missing.
 * @param presentConsumer consumer the is called if the element is there.
 * @param missingCallback callback called if the element is missing.
 */
fun <T> Optional<T>.ifPresentOrElse(presentConsumer: (T) -> Unit, missingCallback: () -> Unit) {
  if (this.isPresent) {
    presentConsumer(this.get())
  } else {
    missingCallback()
  }
}
