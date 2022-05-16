package io.holunda.polyflow.view

import io.holunda.polyflow.view.query.data.DataEntriesForUserQuery
import io.holunda.polyflow.view.query.data.DataEntriesQuery
import io.holunda.polyflow.view.query.data.DataEntriesQueryResult
import io.holunda.polyflow.view.query.data.DataEntryForIdentityQuery
import io.holunda.polyflow.view.query.process.ProcessDefinitionsStartableByUserQuery
import io.holunda.polyflow.view.query.process.ProcessInstanceQueryResult
import io.holunda.polyflow.view.query.process.ProcessInstancesByStateQuery
import io.holunda.polyflow.view.query.process.variable.ProcessVariableQueryResult
import io.holunda.polyflow.view.query.process.variable.ProcessVariablesForInstanceQuery
import io.holunda.polyflow.view.query.task.*
import org.axonframework.messaging.responsetypes.ResponseTypes
import org.axonframework.queryhandling.QueryGateway
import java.util.concurrent.CompletableFuture

/**
 * Extensions on [QueryGateway] that allow type-safe access to the [QueryGateway#query] method
 * without the need to set the correct [ResponseTypes].
 */
object QueryGatewayExt {

  /**
   * @see [DataEntriesForUserQuery]
   */
  fun QueryGateway.dataEntriesForUser(query: DataEntriesForUserQuery): CompletableFuture<DataEntriesQueryResult> = query(
    query,
    ResponseTypes.instanceOf(DataEntriesQueryResult::class.java)
  )

  /**
   * @see [DataEntriesQuery]
   */
  fun QueryGateway.dataEntries(query: DataEntriesQuery): CompletableFuture<DataEntriesQueryResult> = query(
    query,
    ResponseTypes.instanceOf(DataEntriesQueryResult::class.java)
  )

  /**
   * @see [DataEntryForIdentityQuery]
   */
  fun QueryGateway.dataEntryForIdentity(query: DataEntryForIdentityQuery): CompletableFuture<DataEntriesQueryResult> = query(
    query,
    ResponseTypes.instanceOf(DataEntriesQueryResult::class.java)
  )

  /**
   * @see [ProcessVariablesForInstanceQuery]
   */
  fun QueryGateway.processVariablesForInstance(query: ProcessVariablesForInstanceQuery): CompletableFuture<ProcessVariableQueryResult> = query(
    query,
    ResponseTypes.instanceOf(ProcessVariableQueryResult::class.java)
  )

  /**
   * @see [ProcessInstancesByStateQuery]
   */
  fun QueryGateway.processInstancesByState(query: ProcessInstancesByStateQuery): CompletableFuture<ProcessInstanceQueryResult> = query(
    query,
    ResponseTypes.instanceOf(ProcessInstanceQueryResult::class.java)
  )

  /**
   * @see [ProcessDefinitionsStartableByUserQuery"]
   */
  fun QueryGateway.processDefinitionsStartableByUser(query: ProcessDefinitionsStartableByUserQuery): CompletableFuture<List<ProcessDefinition>> = query(
    query,
    ResponseTypes.multipleInstancesOf(ProcessDefinition::class.java)
  )

  /**
   * @see [TasksForUserQuery]
   */
  fun QueryGateway.tasksForUser(query: TasksForUserQuery): CompletableFuture<TaskQueryResult> = query(
    query,
    ResponseTypes.instanceOf(TaskQueryResult::class.java)
  )

  /**
   * @see [TaskForIdQuery]
   */
  fun QueryGateway.taskForId(query: TaskForIdQuery): CompletableFuture<Task?> = query(
    query,
    ResponseTypes.instanceOf(Task::class.java)
  )

  /**
   * @see [TasksForApplicationQuery]
   */
  fun QueryGateway.tasksForApplication(query: TasksForApplicationQuery): CompletableFuture<TaskQueryResult> = query(
    query,
    ResponseTypes.instanceOf(TaskQueryResult::class.java)
  )

  /**
   * @see [TaskWithDataEntriesForIdQuery]
   */
  fun QueryGateway.taskWithDataEntriesForId(query: TaskWithDataEntriesForIdQuery): CompletableFuture<TaskWithDataEntries?> = query(
    query,
    ResponseTypes.instanceOf(TaskWithDataEntries::class.java)
  )

  /**
   * @see [TasksWithDataEntriesForUserQuery]
   */
  fun QueryGateway.tasksWithDataEntriesForUser(query: TasksWithDataEntriesForUserQuery): CompletableFuture<TasksWithDataEntriesQueryResult> = query(
    query,
    ResponseTypes.instanceOf(TasksWithDataEntriesQueryResult::class.java)
  )

  /**
   * @see [TaskCountByApplicationQuery]
   */
  fun QueryGateway.taskCountByApplication(query: TaskCountByApplicationQuery): CompletableFuture<List<ApplicationWithTaskCount>> = query(
    query,
    ResponseTypes.multipleInstancesOf(ApplicationWithTaskCount::class.java)
  )

}
