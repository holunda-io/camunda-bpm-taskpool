package io.holunda.camunda.taskpool.view.query.task

import io.holunda.camunda.taskpool.view.Task
import io.holunda.camunda.taskpool.view.query.QueryResult

/**
 * Result of query for multiple tasks.
 */
data class TaskQueryResult(override val elements: List<Task>) : QueryResult<Task, TaskQueryResult>(elements)
