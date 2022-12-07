package io.holunda.polyflow.view.mongo.service

import com.tngtech.jgiven.Stage
import com.tngtech.jgiven.annotation.*
import com.tngtech.jgiven.integration.spring.JGivenStage
import io.holunda.camunda.taskpool.api.business.DataEntryCreatedEvent
import io.holunda.camunda.taskpool.api.business.DataEntryDeletedEvent
import io.holunda.camunda.taskpool.api.business.DataEntryUpdatedEvent
import io.holunda.camunda.taskpool.api.task.*
import io.holunda.polyflow.view.DataEntry
import io.holunda.polyflow.view.Task
import io.holunda.polyflow.view.TaskWithDataEntries
import io.holunda.polyflow.view.auth.User
import io.holunda.polyflow.view.mongo.MongoViewService
import io.holunda.polyflow.view.query.data.DataEntriesForUserQuery
import io.holunda.polyflow.view.query.data.DataEntryForIdentityQuery
import io.holunda.polyflow.view.query.task.*
import io.toolisticon.testing.jgiven.step
import mu.KLogging
import org.assertj.core.api.Assertions.assertThat
import org.awaitility.Awaitility
import org.awaitility.core.ConditionTimeoutException
import org.axonframework.messaging.MetaData
import org.axonframework.queryhandling.QueryUpdateEmitter
import org.camunda.bpm.engine.variable.VariableMap
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.atLeast
import org.mockito.kotlin.clearInvocations
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import java.util.concurrent.TimeUnit
import java.util.function.Predicate

abstract class PolyflowStage<SELF : PolyflowStage<SELF>> : Stage<SELF>() {
  companion object : KLogging()

  @Autowired
  @ScenarioState
  lateinit var service: MongoViewService

  @Autowired
  @ScenarioState
  lateinit var queryUpdateEmitter: QueryUpdateEmitter

  @ScenarioState
  var emittedQueryUpdates: List<QueryUpdate<Any>> = listOf()

  fun task_created_event_is_received(event: TaskCreatedEngineEvent) = step {
    service.on(event, MetaData.emptyInstance())
  }

  fun task_assign_event_is_received(event: TaskAssignedEngineEvent) = step {
    service.on(event, MetaData.emptyInstance())
  }

  fun task_attributes_update_event_is_received(event: TaskAttributeUpdatedEngineEvent) = step {
    service.on(event, MetaData.emptyInstance())
  }

  fun task_candidate_group_changed_event_is_received(event: TaskCandidateGroupChanged) = step {
    service.on(event, MetaData.emptyInstance())
  }

  fun task_candidate_user_changed_event_is_received(event: TaskCandidateUserChanged) = step {
    service.on(event, MetaData.emptyInstance())
  }

  open fun task_deleted_event_is_received(event: TaskDeletedEngineEvent) = step {
    service.on(event, MetaData.emptyInstance())
  }

  fun time_passes_until_query_update_is_emitted(queryType: Class<out Any> = Object::class.java) = step {
    try {
      Awaitility.await().atMost(2, TimeUnit.SECONDS).until {
        captureEmittedQueryUpdates().any { queryType.isAssignableFrom(it.queryType) }
      }
      logger.info { "Emitted query updates: $emittedQueryUpdates" }
    } catch (e: ConditionTimeoutException) {
      logger.warn { "Query update was not emitted within 2 seconds" }
    }
  }

  fun data_entry_created_event_is_received(event: DataEntryCreatedEvent) = step {
    service.on(event, MetaData.emptyInstance())
  }

  fun data_entry_updated_event_is_received(event: DataEntryUpdatedEvent) = step {
    service.on(event, MetaData.emptyInstance())
  }

  fun data_entry_deleted_event_is_received(event: DataEntryDeletedEvent) = step {
    service.on(event, MetaData.emptyInstance())
  }

  protected fun captureEmittedQueryUpdates(): List<QueryUpdate<Any>> {
    val queryTypeCaptor = argumentCaptor<Class<Any>>()
    val predicateCaptor = argumentCaptor<Predicate<Any>>()
    val updateCaptor = argumentCaptor<Any>()
    verify(queryUpdateEmitter, atLeast(0)).emit(queryTypeCaptor.capture(), predicateCaptor.capture(), updateCaptor.capture())
    clearInvocations(queryUpdateEmitter)

    val foundUpdates = queryTypeCaptor.allValues
      .zip(predicateCaptor.allValues)
      .zip(updateCaptor.allValues) { (queryType, predicate), update ->
        QueryUpdate(queryType, predicate, update)
      }

    emittedQueryUpdates = emittedQueryUpdates + foundUpdates
    return foundUpdates
  }

  data class QueryUpdate<E>(val queryType: Class<E>, val predicate: Predicate<E>, val update: Any)
}

@JGivenStage
class PolyflowGivenStage<SELF : PolyflowGivenStage<SELF>> : PolyflowStage<SELF>() {

  @ProvidedScenarioState(resolution = ScenarioState.Resolution.NAME)
  private lateinit var tasks: List<TaskWithDataEntries>

  @ProvidedScenarioState(resolution = ScenarioState.Resolution.NAME)
  private lateinit var dataEntries: List<DataEntry>

  @AfterStage
  fun resetEmittedQueryUpdates() {
    captureEmittedQueryUpdates()
    emittedQueryUpdates = listOf()
  }

  fun no_task_exists() = step {
    tasks = listOf()
  }

  fun no_data_entry_exists() = step {
    dataEntries = listOf()
  }
}

@JGivenStage
class PolyflowWhenStage<SELF : PolyflowWhenStage<SELF>> : PolyflowStage<SELF>()

@JGivenStage
class PolyflowThenStage<SELF : PolyflowThenStage<SELF>> : PolyflowStage<SELF>() {

  fun task_is_created(task: Task) = step {
    val result = service.query(TaskForIdQuery(task.id)).join()
    assertThat(result).isPresent
    assertThat(result.get()).isEqualTo(task)
  }

  @As("task with id $ is assigned to $")
  fun task_is_assigned_to(taskId: String, assignee: String?) = step {
    val result = service.query(TaskForIdQuery(taskId)).join()
    assertThat(result).isPresent
    assertThat(result.get().assignee).isEqualTo(assignee)
  }

  @As("tasks with payload with ids \$taskIds are visible to \$user")
  fun tasks_with_payload_are_visible_to(user: User, vararg taskIds: String) = step {
    val taskResponse = service.query(TasksWithDataEntriesForUserQuery(user = user, page = 1, size = 100)).join()
    assertThat(taskResponse.elements.map { it.task.id }).containsExactlyElementsOf(taskIds.asIterable())
  }

  fun task_has_candidate_groups(taskId: String, groupIds: Set<String>) = step {
    val result = service.query(TaskForIdQuery(taskId)).join()
    assertThat(result).isPresent
    assertThat(result.get().candidateGroups).isEqualTo(groupIds)
  }

  fun task_has_candidate_users(taskId: String, groupIds: Set<String>) = step {
    val result = service.query(TaskForIdQuery(taskId)).join()
    assertThat(result).isPresent
    assertThat(result.get().candidateUsers).isEqualTo(groupIds)
  }

  fun task_does_not_exist(taskId: String) = step {
    val result = service.query(TaskForIdQuery(taskId)).join()
    assertThat(result).isNotPresent
  }

  fun task_payload_matches(taskId: String, payload: VariableMap) = step {
    val result = service.query(TaskForIdQuery(taskId)).join()
    assertThat(result).isPresent
    assertThat(result.get().payload).isEqualTo(payload)
  }

  fun task_correlations_match(taskId: String, correlations: VariableMap) = step {
    val result = service.query(TaskForIdQuery(taskId)).join()
    assertThat(result).isPresent
    assertThat(result.get().correlations).isEqualTo(correlations)
  }

  fun tasks_visible_to_assignee_or_candidate_user(username: String, expectedTasks: List<Task>) = step {
    val result = service.query(TasksForUserQuery(User(username = username, groups = emptySet()))).join()
    assertThat(result.elements).containsExactlyElementsOf(expectedTasks)
  }

  fun data_entries_visible_to_user(username: String, expectedDataEntries: List<DataEntry>) = step {
    val result = service.query(DataEntriesForUserQuery(User(username = username, groups = emptySet()))).join()
    assertThat(result.elements).containsExactlyElementsOf(expectedDataEntries)
  }

  fun tasks_visible_to_candidate_group(groupName: String, expectedTasks: List<Task>) = step {
    val result = service.query(TasksForUserQuery(User(username = "<unmet>", groups = setOf(groupName)))).join()
    assertThat(result.elements).containsExactlyElementsOf(expectedTasks)
  }

  fun data_entries_visible_to_group(groupName: String, expectedDataEntries: List<DataEntry>) = step {
    val result = service.query(DataEntriesForUserQuery(User(username = "<unmet>", groups = setOf(groupName)))).join()
    assertThat(result.elements).containsExactlyElementsOf(expectedDataEntries)
  }

  fun task_counts_per_application_are(vararg applicationsWithTaskCount: ApplicationWithTaskCount) = step {
    val result = service.query(TaskCountByApplicationQuery()).join()
    assertThat(result).containsOnly(*applicationsWithTaskCount)
  }

  @As("only tasks for application $ are returned")
  fun tasks_are_returned_for_application(applicationName: String, @Hidden taskQueryResult: TaskQueryResult) = step {
    val result = service.query(TasksForApplicationQuery(applicationName)).join()
    assertThat(result).isEqualTo(taskQueryResult)
  }

  @As("the following query updates have been emitted for query \$query: \$updates")
  fun <T : Any> query_updates_have_been_emitted(query: T, vararg updates: Any) = step {
    captureEmittedQueryUpdates()

    val captured = emittedQueryUpdates
      .filter { it.queryType == query::class.java }
      .filter { it.predicate.test(query) }
      .map { it.update }

    assertThat(captured)
      .`as`("Query updates for query $query")
      .usingRecursiveFieldByFieldElementComparatorIgnoringFields("payload", "correlation")
      .contains(*updates)
  }

  fun task_is_not_found_for_user(taskId: String, assignee: String) = step {
    val result = service.query(TasksForUserQuery(User(assignee, setOf()))).join()
    assertThat(result.elements.map { it.id }).doesNotContain(taskId)
  }

  fun no_query_update_has_been_emitted() {
    captureEmittedQueryUpdates()
    assertThat(emittedQueryUpdates).isEmpty()
  }

  fun data_entry_is_created(dataEntry: DataEntry) = step {
    val result = service.query(DataEntryForIdentityQuery(dataEntry.entryType, dataEntry.entryId)).join()
    assertThat(result.elements).containsExactly(dataEntry)
  }

  fun data_entry_does_not_exist(dataEntry: DataEntry) = step {
    val result = service.query(DataEntryForIdentityQuery(dataEntry.entryType, dataEntry.entryId)).join()
    assertThat(result.elements).isEmpty()
  }

}
