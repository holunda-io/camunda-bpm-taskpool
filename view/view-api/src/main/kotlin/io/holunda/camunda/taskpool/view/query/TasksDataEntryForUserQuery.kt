package io.holunda.camunda.taskpool.view.query

import io.holunda.camunda.taskpool.view.auth.User


data class TasksDataEntryForUserQuery(
  val user: User
)
