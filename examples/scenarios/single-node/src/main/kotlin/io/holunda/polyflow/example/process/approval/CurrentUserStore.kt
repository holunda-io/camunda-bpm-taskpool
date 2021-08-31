package io.holunda.polyflow.example.process.approval

/**
 * Hold the username.
 */
data class CurrentUserStore(var username: String? = null) {
  fun clear() {
    this.username = null
  }
}
