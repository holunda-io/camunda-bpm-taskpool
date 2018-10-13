package io.holunda.camunda.taskpool.view.auth

data class User(
  val username: String,
  val groups: Set<String>
)
