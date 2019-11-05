package io.holunda.camunda.taskpool.view.mongo.service

import com.nhaarman.mockito_kotlin.argumentCaptor
import com.nhaarman.mockito_kotlin.atLeast
import com.nhaarman.mockito_kotlin.clearInvocations
import com.nhaarman.mockito_kotlin.verify
import com.tngtech.jgiven.Stage
import com.tngtech.jgiven.annotation.*
import com.tngtech.jgiven.integration.spring.JGivenStage
import io.holunda.camunda.taskpool.api.task.*
import io.holunda.camunda.taskpool.view.Task
import io.holunda.camunda.taskpool.view.TaskWithDataEntries
import io.holunda.camunda.taskpool.view.auth.User
import io.holunda.camunda.taskpool.view.query.task.*
import mu.KLogging
import org.assertj.core.api.Assertions.assertThat
import org.awaitility.Awaitility
import org.awaitility.core.ConditionTimeoutException
import org.axonframework.queryhandling.QueryUpdateEmitter
import org.camunda.bpm.engine.variable.VariableMap
import org.springframework.beans.factory.annotation.Autowired
import java.util.concurrent.TimeUnit
import java.util.function.Predicate

open class TaskPoolStage<SELF : TaskPoolStage<SELF>> : Stage<SELF>() {
  companion object : KLogging()

  @Autowired
  @ScenarioState
  lateinit var testee: TaskPoolMongoService

  @Autowired
  @ScenarioState
  lateinit var queryUpdateEmitter: QueryUpdateEmitter

  @ScenarioState
  var emittedQueryUpdates: List<QueryUpdate<Any>> = listOf()

  open fun task_created_event_is_received(event: TaskCreatedEngineEvent): SELF {
    testee.on(event)
    return self()
  }

  open fun task_assign_event_is_received(event: TaskAssignedEngineEvent): SELF {
    testee.on(event)
    return self()
  }

  open fun task_attributes_update_event_is_received(event: TaskAttributeUpdatedEngineEvent): SELF {
    testee.on(event)
    return self()
  }

  open fun task_candidate_group_changed_event_is_received(event: TaskCandidateGroupChanged): SELF {
    testee.on(event)
    return self()
  }

  open fun task_candidate_user_changed_event_is_received(event: TaskCandidateUserChanged): SELF {
    testee.on(event)
    return self()
  }

  open fun task_deleted_event_is_received(event: TaskDeletedEngineEvent): SELF {
    testee.on(event)
    return self()
  }

  open fun time_passes_until_query_update_is_emitted(queryType: Class<out Any> = Object::class.java): SELF {
    try {
      Awaitility.await().atMost(2, TimeUnit.SECONDS).until {
        captureEmittedQueryUpdates().any { queryType.isAssignableFrom(it.queryType) }
      }
    } catch (e: ConditionTimeoutException) {
      logger.warn { "Query update was not emitted within 2 seconds" }
    }
    return self()
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

    emittedQueryUpdates += foundUpdates
    return foundUpdates
  }

  data class QueryUpdate<E>(val queryType: Class<E>, val predicate: Predicate<E>, val update: Any)
}

@JGivenStage
class TaskPoolGivenStage<SELF : TaskPoolGivenStage<SELF>> : TaskPoolStage<SELF>() {

  @ProvidedScenarioState(resolution = ScenarioState.Resolution.NAME)
  private lateinit var tasks: List<TaskWithDataEntries>

  private val procRef = ProcessReference("instance1", "exec1", "def1", "def-key", "proce1", "app")
  private fun task(i: Int) = TaskWithDataEntries(Task(id = "id$i", sourceReference = procRef, taskDefinitionKey = "task-key-$i", businessKey = "BUS-$i"))

  @AfterStage
  fun resetEmittedQueryUpdates() {
    captureEmittedQueryUpdates()
    emittedQueryUpdates = listOf()
  }

  fun no_task_exists(): SELF {
    tasks = listOf()
    return self()
  }

  @As("$ tasks exist")
  fun tasks_exist(numTasks: Int): SELF {
    tasks = (0 until numTasks).map { task(it) }
    return self()
  }

}

@JGivenStage
class TaskPoolWhenStage<SELF : TaskPoolWhenStage<SELF>> : TaskPoolStage<SELF>() {

  @ExpectedScenarioState(resolution = ScenarioState.Resolution.NAME, required = true)
  private lateinit var tasks: List<TaskWithDataEntries>

  @ProvidedScenarioState(resolution = ScenarioState.Resolution.NAME)
  private var queriedTasks: MutableList<TaskWithDataEntries> = mutableListOf()

  private fun query(page: Int, size: Int) = TasksWithDataEntriesForUserQuery(User("kermit", setOf()), page, size)

  @As("Page $ is queried with a page size of $")
  open fun tasks_queried(page: Int, size: Int): SELF {
    queriedTasks.addAll(TasksWithDataEntriesQueryResult(tasks).slice(query(page, size)).elements)
    return self()
  }

}

@JGivenStage
class TaskPoolThenStage<SELF : TaskPoolThenStage<SELF>> : TaskPoolStage<SELF>() {

  @ExpectedScenarioState(resolution = ScenarioState.Resolution.NAME, required = true)
  private lateinit var tasks: List<TaskWithDataEntries>

  @ExpectedScenarioState(resolution = ScenarioState.Resolution.NAME, required = true)
  private lateinit var queriedTasks: List<TaskWithDataEntries>

  @As("$ tasks are returned")
  fun num_tasks_are_returned(numTasks: Int): SELF {
    assertThat(queriedTasks.size).isEqualTo(numTasks)
    return self()
  }

  @As("all tasks are returned once")
  fun all_tasks_are_returned(): SELF {
    assertThat(queriedTasks).isEqualTo(tasks)
    return self()
  }

  fun task_is_created(task: Task): SELF {
    assertThat(testee.query(TaskForIdQuery(task.id)).join()).isEqualTo(task)
    return self()
  }

  @As("task with id $ is assigned to $")
  fun task_is_assigned_to(taskId: String, assignee: String?): SELF {
    assertThat(testee.query(TaskForIdQuery(taskId)).join()?.assignee).isEqualTo(assignee)
    return self()
  }

  @As("tasks with payload with ids \$taskIds are visible to \$user")
  fun tasks_with_payload_are_visible_to(user: User, vararg taskIds: String): SELF {
    val taskResponse = testee.query(TasksWithDataEntriesForUserQuery(user = user, page = 1, size = 100)).join()
    assertThat(taskResponse.elements.map { it.task.id }).containsExactlyElementsOf(taskIds.asIterable())
    return self()
  }

  fun task_has_candidate_groups(taskId: String, groupIds: Set<String>): SELF {
    assertThat(testee.query(TaskForIdQuery(taskId)).join()?.candidateGroups).isEqualTo(groupIds)
    return self()
  }

  fun task_has_candidate_users(taskId: String, groupIds: Set<String>): SELF {
    assertThat(testee.query(TaskForIdQuery(taskId)).join()?.candidateUsers).isEqualTo(groupIds)
    return self()
  }

  fun task_does_not_exist(taskId: String): SELF {
    assertThat(testee.query(TaskForIdQuery(taskId)).join()).isNull()
    return self()
  }

  fun task_payload_matches(taskId: String, payload: VariableMap): SELF {
    assertThat(testee.query(TaskForIdQuery(taskId)).join()?.payload).isEqualTo(payload)
    return self()
  }

  fun task_correlations_match(taskId: String, correlations: VariableMap): SELF {
    assertThat(testee.query(TaskForIdQuery(taskId)).join()?.correlations).isEqualTo(correlations)
    return self()
  }

  fun tasks_visible_to_assignee_or_candidate_user(username: String, expectedTasks: List<Task>): SELF {
    assertThat(testee.query(TasksForUserQuery(User(username = username, groups = emptySet()))).join().elements).containsExactlyElementsOf(expectedTasks)
    return self()
  }

  fun tasks_visible_to_candidate_group(groupName: String, expectedTasks: List<Task>): SELF {
    assertThat(testee.query(TasksForUserQuery(User(username = "<unmet>", groups = setOf(groupName)))).join().elements).containsExactlyElementsOf(expectedTasks)
    return self()
  }

  fun task_counts_per_application_are(vararg applicationsWithTaskCount: ApplicationWithTaskCount): SELF {
    assertThat(testee.query(TaskCountByApplicationQuery()).join()).containsOnly(*applicationsWithTaskCount)
    return self()
  }

  @As("only tasks for application $ are returned")
  fun tasks_are_returned_for_application(applicationName: String, @Hidden taskQueryResult: TaskQueryResult): SELF {
    assertThat(testee.query(TasksForApplicationQuery(applicationName)).join()).isEqualTo(taskQueryResult)
    return self()
  }

  @As("the following query updates have been emitted for query \$query: \$updates")
  fun <T : Any> query_updates_have_been_emitted(query: T, vararg updates: Any): SELF {
    captureEmittedQueryUpdates()
    assertThat(emittedQueryUpdates
      .filter { it.queryType == query::class.java }
      .filter { it.predicate.test(query) }
      .map { it.update })
      .`as`("Query updates for query $query")
      .contains(*updates)
    return self()
  }

  fun no_query_update_has_been_emitted() {
    captureEmittedQueryUpdates()
    assertThat(emittedQueryUpdates).isEmpty()
  }

}
