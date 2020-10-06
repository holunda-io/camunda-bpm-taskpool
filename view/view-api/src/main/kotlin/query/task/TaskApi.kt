package io.holunda.camunda.taskpool.view.query.task

import io.holunda.camunda.taskpool.view.Task
import io.holunda.camunda.taskpool.view.TaskWithDataEntries
import io.holunda.camunda.taskpool.view.query.task.*

/**
 * API to access task projection.
 */
interface TaskApi {

  /**
   * Retrieve tasks with correlated data entries for given user.
   */
  fun query(query: TasksWithDataEntriesForUserQuery): TasksWithDataEntriesQueryResult

  /**
   * Retrieve task with correlated data entries by given id.
   */
  fun query(query: TaskWithDataEntriesForIdQuery): TaskWithDataEntries?

  /**
   * Count tasks and group by application.
   */
  fun query(query: TaskCountByApplicationQuery): List<ApplicationWithTaskCount>

  /**
   * Retrieve tasks for given user.
   */
  fun query(query: TasksForUserQuery): TaskQueryResult

  /**
   * Retrieve task by given id.
   */
  fun query(query: TaskForIdQuery): Task?

  /**
   * Retrieve tasks for given application.
   */
  fun query(query: TasksForApplicationQuery): TaskQueryResult

}
