package io.holunda.polyflow.view

import io.holixon.axon.gateway.query.RevisionQueryParameters
import io.holunda.polyflow.view.query.data.*
import io.holunda.polyflow.view.query.process.ProcessDefinitionsStartableByUserQuery
import io.holunda.polyflow.view.query.process.ProcessInstanceQueryResult
import io.holunda.polyflow.view.query.process.ProcessInstancesByStateQuery
import io.holunda.polyflow.view.query.process.variable.ProcessVariableQueryResult
import io.holunda.polyflow.view.query.process.variable.ProcessVariablesForInstanceQuery
import io.holunda.polyflow.view.query.task.*
import org.axonframework.messaging.responsetypes.ResponseTypes
import org.axonframework.queryhandling.QueryGateway
import java.util.*
import java.util.concurrent.CompletableFuture

/**
 * Extensions on [QueryGateway] that allow type-safe access to the [QueryGateway#query] method
 * without the need to set the correct [ResponseTypes].
 */
object QueryGatewayExt {

  /**
   * @see [DataEntriesForUserQuery]
   */
  fun QueryGateway.dataEntriesForUser(
    query: DataEntriesForUserQuery,
    revisionQuery: RevisionQueryParameters? = null
  ): CompletableFuture<DataEntriesQueryResult> =
    DataEntryQueryClient(this).query(query, revisionQuery)


  /**
   * @see [DataEntriesQuery]
   */
  fun QueryGateway.dataEntries(query: DataEntriesQuery, revisionQuery: RevisionQueryParameters? = null): CompletableFuture<DataEntriesQueryResult> =
    DataEntryQueryClient(this).query(query, revisionQuery)

  /**
   * @see [DataEntryForIdentityQuery]
   */
  fun QueryGateway.dataEntryForIdentity(
    query: DataEntryForIdentityQuery,
    revisionQuery: RevisionQueryParameters? = null
  ): CompletableFuture<DataEntry> =
    DataEntryQueryClient(this).query(query, revisionQuery)

  /**
   * @see [DataEntriesForDataEntryTypeQuery]
   */
  fun QueryGateway.dataEntriesForDataEntryType(
    query: DataEntriesForDataEntryTypeQuery,
    revisionQuery: RevisionQueryParameters? = null
  ): CompletableFuture<DataEntriesQueryResult> =
    DataEntryQueryClient(this).query(query, revisionQuery)

  /**
   * @see [ProcessVariablesForInstanceQuery]
   */
  fun QueryGateway.processVariablesForInstance(query: ProcessVariablesForInstanceQuery): CompletableFuture<ProcessVariableQueryResult> =
    ProcessVariableQueryClient(this).query(query)

  /**
   * @see [ProcessInstancesByStateQuery]
   */
  fun QueryGateway.processInstancesByState(query: ProcessInstancesByStateQuery): CompletableFuture<ProcessInstanceQueryResult> =
    ProcessInstanceQueryClient(this).query(query)

  /**
   * @see [ProcessDefinitionsStartableByUserQuery"]
   */
  fun QueryGateway.processDefinitionsStartableByUser(query: ProcessDefinitionsStartableByUserQuery): CompletableFuture<List<ProcessDefinition>> =
    ProcessDefinitionQueryClient(this).query(query)

  /**
   * @see [TasksForUserQuery]
   */
  fun QueryGateway.tasksForUser(query: TasksForUserQuery): CompletableFuture<TaskQueryResult> =
    TaskQueryClient(this).query(query)

  /**
   * @see [TaskForIdQuery]
   */
  fun QueryGateway.taskForId(query: TaskForIdQuery): CompletableFuture<Optional<Task>> =
    TaskQueryClient(this).query(query)

  /**
   * @see [TasksForApplicationQuery]
   */
  fun QueryGateway.tasksForApplication(query: TasksForApplicationQuery): CompletableFuture<TaskQueryResult> =
    TaskQueryClient(this).query(query)

  /**
   * @see [TaskWithDataEntriesForIdQuery]
   */
  fun QueryGateway.taskWithDataEntriesForId(query: TaskWithDataEntriesForIdQuery): CompletableFuture<Optional<TaskWithDataEntries>> =
    TaskQueryClient(this).query(query)

  /**
   * @see [TasksWithDataEntriesForUserQuery]
   */
  fun QueryGateway.tasksWithDataEntriesForUser(query: TasksWithDataEntriesForUserQuery): CompletableFuture<TasksWithDataEntriesQueryResult> =
    TaskQueryClient(this).query(query)

  /**
   * @see [TaskCountByApplicationQuery]
   */
  fun QueryGateway.taskCountByApplication(query: TaskCountByApplicationQuery): CompletableFuture<List<ApplicationWithTaskCount>> =
    TaskQueryClient(this).query(query)

  /**
   * @see [AllTasksQuery]
   */
  fun QueryGateway.allTasks(query: AllTasksQuery): CompletableFuture<TaskQueryResult> = TaskQueryClient(this).query(query)

  /**
   * @see [AllTasksWithDataEntriesQuery]
   */
  fun QueryGateway.allTasksWithDataEntries(query: AllTasksWithDataEntriesQuery): CompletableFuture<TasksWithDataEntriesQueryResult> =
    TaskQueryClient(this).query(query)

  /**
   * @see [TasksForGroupQuery]
   */
  fun QueryGateway.tasksForGroup(query: TasksForGroupQuery): CompletableFuture<TaskQueryResult> = TaskQueryClient(this).query(query)

  /**
   * @see [TasksWithDataEntriesForGroupQuery]
   */
  fun QueryGateway.tasksWithDataEntriesForGroup(query: TasksWithDataEntriesForGroupQuery): CompletableFuture<TasksWithDataEntriesQueryResult> =
    TaskQueryClient(this).query(query)

  /**
   * @see [TasksForGroupQuery]
   */
  fun QueryGateway.tasksForCandidateUserAndGroup(query: TasksForCandidateUserAndGroupQuery): CompletableFuture<TaskQueryResult> = TaskQueryClient(this).query(query)

}
