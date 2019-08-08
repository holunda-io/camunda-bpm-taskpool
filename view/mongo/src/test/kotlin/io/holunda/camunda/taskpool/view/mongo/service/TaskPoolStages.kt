package io.holunda.camunda.taskpool.view.mongo.service

import com.mongodb.MongoClient
import com.tngtech.jgiven.Stage
import com.tngtech.jgiven.annotation.*
import com.tngtech.jgiven.integration.spring.JGivenStage
import de.flapdoodle.embed.mongo.MongodExecutable
import de.flapdoodle.embed.mongo.MongodProcess
import io.holunda.camunda.taskpool.api.task.*
import io.holunda.camunda.taskpool.view.Task
import io.holunda.camunda.taskpool.view.TaskWithDataEntries
import io.holunda.camunda.taskpool.view.auth.User
import io.holunda.camunda.taskpool.view.mongo.utils.MongoLauncher
import io.holunda.camunda.taskpool.view.query.QueryResult
import io.holunda.camunda.taskpool.view.query.task.TaskForIdQuery
import io.holunda.camunda.taskpool.view.query.task.TasksForUserQuery
import io.holunda.camunda.taskpool.view.query.task.TasksWithDataEntriesForUserQuery
import io.holunda.camunda.taskpool.view.query.task.TasksWithDataEntriesQueryResult
import org.assertj.core.api.Assertions.assertThat
import org.axonframework.extensions.mongo.DefaultMongoTemplate
import org.camunda.bpm.engine.variable.VariableMap
import org.springframework.beans.factory.annotation.Autowired

open class TaskPoolStage<SELF : TaskPoolStage<SELF>> : Stage<SELF>() {

  @Autowired
  @ScenarioState
  lateinit var testee: TaskPoolMongoService

  private var mongod: MongodProcess? = null
  private var mongoExecutable: MongodExecutable? = null

  @BeforeScenario
  fun initMongo() {
    mongoExecutable = MongoLauncher.prepareExecutable()
    mongod = mongoExecutable!!.start()
    if (mongod == null) {
      // we're using an existing mongo instance. Make sure it's clean
      val template = DefaultMongoTemplate.builder().mongoDatabase(MongoClient()).build()
      template.trackingTokensCollection().drop()
      template.eventCollection().drop()
      template.snapshotCollection().drop()
    }
  }

  @AfterScenario
  fun stop() {
    if (mongod != null) {
      mongod!!.stop()
    }
    if (mongoExecutable != null) {
      mongoExecutable!!.stop()
    }
  }

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

}

@JGivenStage
class TaskPoolGivenStage<SELF : TaskPoolGivenStage<SELF>> : TaskPoolStage<SELF>() {

  @ProvidedScenarioState(resolution = ScenarioState.Resolution.NAME)
  private lateinit var tasks: List<TaskWithDataEntries>

  private val procRef = ProcessReference("instance1", "exec1", "def1", "def-key", "proce1", "app")
  private fun task(i: Int) = TaskWithDataEntries(Task(id = "id$i", sourceReference = procRef, taskDefinitionKey = "task-key-$i", businessKey = "BUS-$i"))

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
    assertThat(testee.query(TaskForIdQuery(task.id))).isEqualTo(task)
    return self()
  }

  @As("task with id $ is assigned to $")
  fun task_is_assigned_to(taskId: String, assignee: String?): SELF {
    assertThat(testee.query(TaskForIdQuery(taskId))?.assignee).isEqualTo(assignee)
    return self()
  }

  @As("tasks with payload with ids \$taskIds are visible to \$user")
  fun tasks_with_payload_are_visible_to(user: User, vararg taskIds: String): SELF {
    val taskResponse = testee.query(TasksWithDataEntriesForUserQuery(user = user, page = 1, size = 100))
    assertThat(taskResponse.elements.map { it.task.id }).containsExactlyElementsOf(taskIds.asIterable())
    return self()
  }

  fun task_has_candidate_groups(taskId: String, groupIds: Set<String>): SELF {
    assertThat(testee.query(TaskForIdQuery(taskId))?.candidateGroups).isEqualTo(groupIds)
    return self()
  }

  fun task_has_candidate_users(taskId: String, groupIds: Set<String>): SELF {
    assertThat(testee.query(TaskForIdQuery(taskId))?.candidateUsers).isEqualTo(groupIds)
    return self()
  }

  fun task_payload_matches(taskId: String, payload: VariableMap): SELF {
    assertThat(testee.query(TaskForIdQuery(taskId))?.payload).isEqualTo(payload)
    return self()
  }

  fun task_correlations_match(taskId: String, correlations: VariableMap): SELF {
    assertThat(testee.query(TaskForIdQuery(taskId))?.correlations).isEqualTo(correlations)
    return self()
  }

  fun tasks_visible_to_assignee_or_candidate_user(username: String, expectedTasks: List<Task>): SELF {
    assertThat(testee.query(TasksForUserQuery(User(username = username, groups = emptySet()))).elements).containsExactlyElementsOf(expectedTasks)
    return self()
  }

  fun tasks_visible_to_candidate_group(groupName: String, expectedTasks: List<Task>): SELF {
    assertThat(testee.query(TasksForUserQuery(User(username = "<unmet>", groups = setOf(groupName)))).elements).containsExactlyElementsOf(expectedTasks)
    return self()
  }

}
