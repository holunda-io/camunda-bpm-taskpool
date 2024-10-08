package io.holunda.polyflow.view.simple.service

import com.tngtech.jgiven.Stage
import com.tngtech.jgiven.annotation.*
import io.holunda.camunda.taskpool.api.task.*
import io.holunda.polyflow.view.Task
import io.holunda.polyflow.view.TaskWithDataEntries
import io.holunda.polyflow.view.auth.User
import io.holunda.polyflow.view.query.task.*
import io.toolisticon.testing.jgiven.JGivenKotlinStage
import io.toolisticon.testing.jgiven.step
import org.assertj.core.api.Assertions.assertThat
import org.axonframework.queryhandling.QueryUpdateEmitter
import org.camunda.bpm.engine.variable.VariableMap
import org.camunda.bpm.engine.variable.Variables.createVariables
import org.mockito.Mockito

abstract class AbstractSimpleTaskPoolStage<SELF : AbstractSimpleTaskPoolStage<SELF>> : Stage<SELF>() {

  @ScenarioState
  lateinit var simpleTaskPoolService: SimpleTaskPoolService

  @BeforeScenario
  fun init() {
    simpleTaskPoolService = SimpleTaskPoolService(Mockito.mock(QueryUpdateEmitter::class.java))
  }

  fun task_created_event_is_received(event: TaskCreatedEngineEvent) = step {
    simpleTaskPoolService.on(event)
  }

  fun task_assign_event_is_received(event: TaskAssignedEngineEvent) = step {
    simpleTaskPoolService.on(event)
  }

  fun task_attributes_update_event_is_received(event: TaskAttributeUpdatedEngineEvent) = step {
    simpleTaskPoolService.on(event)
  }

  fun task_candidate_group_changed_event_is_received(event: TaskCandidateGroupChanged) = step {
    simpleTaskPoolService.on(event)
  }

  fun task_candidate_user_changed_event_is_received(event: TaskCandidateUserChanged) = step {
    simpleTaskPoolService.on(event)
  }

}

@JGivenKotlinStage
class SimpleTaskPoolGivenStage<SELF : SimpleTaskPoolGivenStage<SELF>> : AbstractSimpleTaskPoolStage<SELF>() {

  @ProvidedScenarioState(resolution = ScenarioState.Resolution.NAME)
  private var tasks: List<TaskWithDataEntries> = listOf()

  private fun procRef(applicationName: String = "app") = ProcessReference("instance1", "exec1", "def1", "def-key", "proce1", applicationName)

  private fun task(i: Int, applicationName: String = "app") = TestTaskData(
    id = "id$i",
    sourceReference = procRef(applicationName),
    taskDefinitionKey = "task-key-$i",
    businessKey = "BUS-$i",
    payload = createVariables().apply {
      put("payloadIdInt", i)
      put("payloadIdString", "$i")
      put("payloadComplex.attr1", "$i")
    }
  )

  fun no_task_exists() = step {
    tasks = listOf()
  }

  @As("$ tasks exist")
  fun tasks_exist(numTasks: Int, taskDefinitionKey: String? = null) = step {
    tasks = (0 until numTasks).map { task(it) }.also { createTasksInService(it) }.map { TaskWithDataEntries(it.asTask()) }
    return self()
  }

  @As("$ tasks exist from application $")
  fun tasks_exist_from_application(numTasks: Int, applicationName: String) = step {
    tasks += (tasks.size until tasks.size + numTasks).map { task(it, applicationName) }.also { createTasksInService(it) }.map { TaskWithDataEntries(it.asTask()) }
  }

  private fun createTasksInService(tasks: List<TestTaskData>) {
    tasks.forEach { simpleTaskPoolService.on(it.asTaskCreatedEngineEvent()) }
  }

}

@JGivenKotlinStage
class SimpleTaskPoolWhenStage<SELF : SimpleTaskPoolWhenStage<SELF>> : AbstractSimpleTaskPoolStage<SELF>() {

  @ExpectedScenarioState(resolution = ScenarioState.Resolution.NAME, required = true)
  private lateinit var tasks: List<TaskWithDataEntries>

  @ProvidedScenarioState(resolution = ScenarioState.Resolution.NAME)
  private var attributeNames: List<String> = listOf()

  @ProvidedScenarioState(resolution = ScenarioState.Resolution.NAME)
  private var attributeValues: List<Any> = listOf()

  @ProvidedScenarioState(resolution = ScenarioState.Resolution.NAME)
  private var queriedTasks: MutableList<TaskWithDataEntries> = mutableListOf()

  @ProvidedScenarioState(resolution = ScenarioState.Resolution.NAME)
  private var returnedTaskCounts: List<ApplicationWithTaskCount> = listOf()

  @ProvidedScenarioState(resolution = ScenarioState.Resolution.NAME)
  private var returnedTasksForApplication = TaskQueryResult(listOf())

  private fun query(page: Int, size: Int) = TasksWithDataEntriesForUserQuery(User("kermit", setOf()), true, page, size)
  private fun filterQuery(sort: List<String>, filters: List<String>) = TasksForUserQuery(assignedToMeOnly = false, user = User("kermit", setOf()), filters = filters, sort = sort)

  @As("Page $ is queried with a page size of $")
  fun page_is_queried(page: Int, size: Int) = step {
    queriedTasks.addAll(TasksWithDataEntriesQueryResult(tasks).slice(query(page, size)).elements)
  }

  @As("Task count by application is queried")
  fun task_count_queried() = step {
    returnedTaskCounts = simpleTaskPoolService.query(TaskCountByApplicationQuery())
  }

  @As("Tasks are queried for application $")
  fun tasks_queried_for_application(applicationName: String) = step {
    returnedTasksForApplication = simpleTaskPoolService.query(TasksForApplicationQuery(applicationName))
  }

  @As("Tasks are queried with filter $")
  fun tasks_are_queried(filters: List<String>) = step {
    queriedTasks.addAll(simpleTaskPoolService.query(filterQuery(listOf("+createdDate"), filters)).elements.map { TaskWithDataEntries(it) })
  }

  @As("All tasks are queried with filter $")
  fun all_tasks_are_queried(filters: List<String>, sort: List<String> = listOf("+name")) = step {
    queriedTasks.addAll(simpleTaskPoolService.query(AllTasksQuery(sort = sort, filters = filters)).elements.map { TaskWithDataEntries(it) })
  }

  @As("Task Attribute Names are queried for user $ with group $")
  fun task_attribute_names_are_queried(user: String, group: String) = step {
    attributeNames = simpleTaskPoolService.query(TaskAttributeNamesQuery(user = User(user, setOf(group)))).elements
  }

  @As("Task Attribute Names are queried for assigned user $ with group $")
  fun task_attribute_names_are_queried_for_assigned_user(user: String, group: String?) = step {
    attributeNames = simpleTaskPoolService.query(TaskAttributeNamesQuery(user = User(user, setOfNotNull(group)), assignedToMeOnly = true)).elements
  }

  @As("Task Attribute Values are queried for name $ and user $ with group $")
  fun task_attribute_values_are_queried(name: String, user: String, group: String) = step {
    attributeValues = simpleTaskPoolService.query(TaskAttributeValuesQuery(attributeName = name, user = User(user, setOf(group)))).elements
  }

  @As("Task Attribute Values are queried for name $ and assigned user $ with group $")
  fun task_attribute_values_are_queried_for_assigned_user(name: String, user: String, group: String?) = step {
    attributeValues = simpleTaskPoolService.query(TaskAttributeValuesQuery(attributeName = name, user = User(user, setOfNotNull(group)), assignedToMeOnly = true)).elements
  }

}

@JGivenKotlinStage
class SimpleTaskPoolThenStage<SELF : SimpleTaskPoolThenStage<SELF>> : AbstractSimpleTaskPoolStage<SELF>() {

  @ExpectedScenarioState(resolution = ScenarioState.Resolution.NAME, required = true)
  lateinit var tasks: List<TaskWithDataEntries>

  @ExpectedScenarioState(resolution = ScenarioState.Resolution.NAME, required = true)
  private lateinit var queriedTasks: List<TaskWithDataEntries>

  @ExpectedScenarioState(resolution = ScenarioState.Resolution.NAME, required = true)
  private lateinit var returnedTaskCounts: List<ApplicationWithTaskCount>

  @ExpectedScenarioState(resolution = ScenarioState.Resolution.NAME, required = true)
  private lateinit var returnedTasksForApplication: TaskQueryResult

  @ExpectedScenarioState(resolution = ScenarioState.Resolution.NAME)
  private var attributeNames: List<String> = listOf()

  @ExpectedScenarioState(resolution = ScenarioState.Resolution.NAME)
  private var attributeValues: List<Any> = listOf()

  @As("$ tasks are returned")
  fun num_tasks_are_returned(numTasks: Int) = step {
    assertThat(queriedTasks.size).isEqualTo(numTasks)
  }

  @As("expected tasks are returned")
  fun tasks_are_returned(@Hidden expected: List<TaskWithDataEntries>) = step {
    assertThat(queriedTasks).containsExactlyInAnyOrderElementsOf(expected)
  }

  @As("all tasks are returned once")
  fun all_tasks_are_returned() = step {
    assertThat(queriedTasks).isEqualTo(tasks)
  }

  @As("all tasks are returned and sorted once")
  fun <R : Comparable<R>> all_task_are_returned_and_sorted_by(reversed: Boolean = false , selector: (TaskWithDataEntries) -> R?) {
    if (reversed) {
    assertThat(queriedTasks).isEqualTo(tasks.sortedByDescending(selector))
    } else {
      assertThat(queriedTasks).isEqualTo(tasks.sortedBy(selector))
    }
  }


  @As("tasks $ are returned for application")
  fun tasks_are_returned_for_application(@Hidden vararg expectedTasks: Task) = step {
    assertThat(returnedTasksForApplication.elements).containsExactlyInAnyOrder(*expectedTasks)
  }

  @As("attribute names $ are returned")
  fun attribute_names_are_returned(count: Int) = step {
    assertThat(attributeNames).hasSize(count)
  }

  @As("attribute values $ are returned")
  fun attribute_values_are_returned(count: Int) = step {
    assertThat(attributeValues).hasSize(count)
  }

  fun task_is_created(task: Task) = step {
    val result = simpleTaskPoolService.query(TaskForIdQuery(task.id))
    assertThat(result).isPresent
    assertThat(result.get()).isEqualTo(task)
  }

  @As("task with id $ is assigned to $")
  fun task_is_assigned_to(taskId: String, assignee: String?) = step {
    val result = simpleTaskPoolService.query(TaskForIdQuery(taskId))
    assertThat(result).isPresent
    assertThat(result.get().assignee).isEqualTo(assignee)
  }

  fun task_has_candidate_groups(taskId: String, groupIds: Set<String>) = step {
    val result = simpleTaskPoolService.query(TaskForIdQuery(taskId))
    assertThat(result).isPresent
    assertThat(result.get().candidateGroups).isEqualTo(groupIds)
  }

  fun task_has_candidate_users(taskId: String, groupIds: Set<String>) = step {
    val result = simpleTaskPoolService.query(TaskForIdQuery(taskId))
    assertThat(result).isPresent
    assertThat(result.get().candidateUsers).isEqualTo(groupIds)
  }

  fun task_payload_matches(taskId: String, payload: VariableMap) = step {
    val result = simpleTaskPoolService.query(TaskForIdQuery(taskId))
    assertThat(result).isPresent
    assertThat(result.get().payload).isEqualTo(payload)
  }

  fun task_correlations_match(taskId: String, correlations: VariableMap) = step {
    val result = simpleTaskPoolService.query(TaskForIdQuery(taskId))
    assertThat(result).isPresent
    assertThat(result.get().correlations).isEqualTo(correlations)
  }

  fun task_counts_are(vararg entries: ApplicationWithTaskCount) = step {
    assertThat(returnedTaskCounts).containsOnly(*entries)
  }

  fun task_does_not_exist(taskId: String) = step {
    val result = simpleTaskPoolService.query(TaskForIdQuery(taskId))
    assertThat(result).isNotPresent
  }

  fun task_is_not_found_for_user(userId: String, taskId: String) = step {
    val result = simpleTaskPoolService.query(TasksForUserQuery(assignedToMeOnly = false, user = User(username = userId, groups = setOf())))
    assertThat(result.elements.map { it.id }).doesNotContain(taskId)
  }


}
