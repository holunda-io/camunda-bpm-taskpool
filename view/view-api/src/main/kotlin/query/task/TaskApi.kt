package io.holunda.polyflow.view.query.task

import io.holunda.polyflow.view.Task
import io.holunda.polyflow.view.TaskWithDataEntries
import java.util.*

/**
 * API to access task projection.
 */
interface TaskApi {

  /**
   * Retrieve tasks with correlated data entries for given user.
   */
  fun query(query: TasksWithDataEntriesForUserQuery): TasksWithDataEntriesQueryResult

  /**
   * Retrieve tasks with correlated data entries for given user.
   */
  fun query(query: TasksWithDataEntriesForGroupQuery): TasksWithDataEntriesQueryResult

  /**
   * Retrieve task with correlated data entries by given id.
   */
  fun query(query: TaskWithDataEntriesForIdQuery): Optional<TaskWithDataEntries>

  /**
   * Retrieve all tasks with correlated data entries.
   */
  fun query(query: AllTasksWithDataEntriesQuery): TasksWithDataEntriesQueryResult

  /**
   * Count tasks and group by application.
   */
  fun query(query: TaskCountByApplicationQuery): List<ApplicationWithTaskCount>

  /**
   * Retrieve tasks for given user.
   */
  fun query(query: TasksForUserQuery): TaskQueryResult

  /**
   * Retrieve tasks for given user's groups.
   */
  fun query(query: TasksForGroupQuery): TaskQueryResult

  /**
   * Retrieves tasks for given user's groups and user's being candidates.
   */
  fun query(query: TasksForCandidateUserAndGroupQuery): TaskQueryResult

  /**
   * Retrieve task by given id.
   */
  fun query(query: TaskForIdQuery): Optional<Task>

  /**
   * Retrieve tasks for given application.
   */
  fun query(query: TasksForApplicationQuery): TaskQueryResult

  /**
   * Retrieve all tasks.
   */
  fun query(query: AllTasksQuery): TaskQueryResult

  /**
   * Retrieves all task attribute names
   */
  fun query(query: TaskAttributeNamesQuery): TaskAttributeNamesQueryResult

  /**
   * Retrieves all task attribute values for an attribute name
   */
  fun query(query: TaskAttributeValuesQuery): TaskAttributeValuesQueryResult
}
