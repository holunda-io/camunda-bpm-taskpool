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
    id ="id$i",
    sourceReference = procRef(applicationName),
    taskDefinitionKey = "task-key-$i",
    businessKey = "BUS-$i",
    payload = createVariables().apply {
      put("payloadIdInt", i)
      put("payloadIdString", "$i")
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
  private var queriedTasks: MutableList<TaskWithDataEntries> = mutableListOf()

  @ProvidedScenarioState(resolution = ScenarioState.Resolution.NAME)
  private var returnedTaskCounts: List<ApplicationWithTaskCount> = listOf()

  @ProvidedScenarioState(resolution = ScenarioState.Resolution.NAME)
  private var returnedTasksForApplication = TaskQueryResult(listOf())

  private fun query(page: Int, size: Int) = TasksWithDataEntriesForUserQuery(User("kermit", setOf()), page, size)
  private fun filterQuery(sort: String, filters: List<String>) = TasksForUserQuery(user = User("kermit", setOf()), filters = filters, sort = sort)

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
    queriedTasks.addAll(simpleTaskPoolService.query(filterQuery("+name", filters)).elements.map { TaskWithDataEntries(it) })
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

  @As("$ tasks are returned")
  fun num_tasks_are_returned(numTasks: Int) = step {
    assertThat(queriedTasks.size).isEqualTo(numTasks)
  }

  @As("expected tasks are returned")
  fun tasks_are_returned(@Hidden expected: List<TaskWithDataEntries>) = step {
    assertThat(queriedTasks).containsExactlyInAnyOrderElementsOf( expected )
  }

  @As("all tasks are returned once")
  fun all_tasks_are_returned() = step {
    assertThat(queriedTasks).isEqualTo(tasks)
  }

  @As("tasks $ are returned for application")
  fun tasks_are_returned_for_application(@Hidden vararg expectedTasks: Task) = step {
    assertThat(returnedTasksForApplication.elements).containsExactlyInAnyOrder(*expectedTasks)
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

  fun task_does_not_exist(taskId: String) = step  {
    val result = simpleTaskPoolService.query(TaskForIdQuery(taskId))
    assertThat(result).isNotPresent
  }

  fun task_is_not_found_for_user(userId: String, taskId: String) = step {
    val result = simpleTaskPoolService.query(TasksForUserQuery(User(userId, setOf())))
    assertThat(result.elements.map { it.id }).doesNotContain(taskId)
  }


}
