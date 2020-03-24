package io.holunda.camunda.taskpool.view.mongo.service

import com.tngtech.jgiven.integration.spring.SpringRuleScenarioTest
import io.holunda.camunda.taskpool.api.business.CorrelationMap
import io.holunda.camunda.taskpool.api.task.*
import io.holunda.camunda.taskpool.view.DataEntry
import io.holunda.camunda.taskpool.view.Task
import io.holunda.camunda.taskpool.view.TaskWithDataEntries
import io.holunda.camunda.taskpool.view.auth.User
import io.holunda.camunda.taskpool.view.mongo.TaskpoolMongoTestApplication
import io.holunda.camunda.taskpool.view.mongo.utils.MongoLauncher
import io.holunda.camunda.taskpool.view.query.task.ApplicationWithTaskCount
import io.holunda.camunda.taskpool.view.query.task.TaskCountByApplicationQuery
import io.holunda.camunda.taskpool.view.query.task.TaskQueryResult
import io.holunda.camunda.taskpool.view.query.task.TasksForUserQuery
import io.holunda.camunda.taskpool.view.query.task.TasksWithDataEntriesForUserQuery
import org.camunda.bpm.engine.variable.VariableMap
import org.camunda.bpm.engine.variable.Variables
import org.junit.*
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource
import java.util.*

@SpringBootTest(classes = [TaskpoolMongoTestApplication::class])
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
abstract class TaskPoolMongoServiceITestBase : SpringRuleScenarioTest<TaskPoolGivenStage<*>, TaskPoolWhenStage<*>, TaskPoolThenStage<*>>() {

  @Test
  fun `a task is created on receiving TaskCreatedEngineEvent`() {

    val testData = TestTaskData(id = "some-id", assignee = "kermit")
    val expected = testData.asTask()

    given()
      .no_task_exists()

    `when`()
      .task_created_event_is_received(testData.asTaskCreatedEngineEvent())
      .and()
      .time_passes_until_query_update_is_emitted()

    then()
      .task_is_created(expected)
      .and()
      .tasks_visible_to_assignee_or_candidate_user("kermit", listOf(expected))
      .and()
      .tasks_visible_to_assignee_or_candidate_user("piggy", listOf(expected))
      .and()
      .tasks_visible_to_candidate_group("muppetshow", listOf(expected))
      .and()
      .query_updates_have_been_emitted(TasksForUserQuery(User("kermit", setOf("muppetshow"))), expected)
      .and()
      .query_updates_have_been_emitted(TasksWithDataEntriesForUserQuery(User("kermit", setOf("muppetshow"))), expected.withDataEntries())
  }

  @Test
  fun `a task is assigned on receiving TaskAssignedEngineEvent`() {
    given()
      .no_task_exists()
      .and()
      .task_created_event_is_received(TestTaskData(id = "some-id").asTaskCreatedEngineEvent())

    `when`()
      .task_assign_event_is_received(TestTaskData(id = "some-id", assignee = "kermit").asTaskAssignedEngineEvent())
      .and()
      .time_passes_until_query_update_is_emitted()

    then()
      .task_is_assigned_to("some-id", "kermit")
      .and()
      .task_is_created(TestTaskData(id = "some-id", assignee = "kermit").asTask())
      .and()
      .query_updates_have_been_emitted(TasksForUserQuery(User("kermit", setOf("muppetshow"))), TestTaskData(id = "some-id", assignee = "kermit").asTask())
  }

  @Test
  fun `do not lose task data by task assignment`() {
    given()
      .no_task_exists()
      .and()
      .task_created_event_is_received(TestTaskData(id = "some-id").asTaskCreatedEngineEvent())

    `when`()
      .task_assign_event_is_received(TestTaskData(id = "some-id", assignee = "kermit").asTaskAssignedEngineEvent())

    then()
      .task_payload_matches("some-id", Variables.fromMap(mapOf(Pair("variableKey", "variableValue"))))
      .and()
      .task_correlations_match("some-id", Variables.fromMap(mapOf(Pair("correlationKey", "correlationValue"))))
  }

  @Test
  fun `do not lose assignee by task attribute update`() {
    given()
      .no_task_exists()
      .and()
      .task_created_event_is_received(TestTaskData(id = "some-id").asTaskCreatedEngineEvent())
      .and()
      .task_assign_event_is_received(TestTaskData(id = "some-id", assignee = "kermit").asTaskAssignedEngineEvent())

    `when`()
      .task_attributes_update_event_is_received(TestTaskData(id = "some-id", followUpDate = Date(1234699999L)).asTaskAttributeUpdatedEvent())

    then()
      .task_is_assigned_to("some-id", "kermit")
  }

  @Test
  fun `a non-existing task is not assigned`() {
    given()
      .no_task_exists()

    `when`()
      .task_assign_event_is_received(TestTaskData(id = "some-id", assignee = "kermit").asTaskAssignedEngineEvent())

    then()
      .task_is_assigned_to("some-id", null)
      .and()
      .no_query_update_has_been_emitted()
  }

  @Test
  fun `candidate groups are updated`() {
    val testTaskData = TestTaskData(id = "some-id", candidateGroups = setOf("muppetshow"))

    given()
      .no_task_exists()
      .and()
      .task_created_event_is_received(testTaskData.asTaskCreatedEngineEvent())

    `when`()
      .task_candidate_group_changed_event_is_received(TestTaskData(id = "some-id").asCandidateGroupChangedEvent("muppetshow", CamundaTaskEvent.CANDIDATE_GROUP_DELETE))
      .and()
      .time_passes_until_query_update_is_emitted()
      .and()
      .task_candidate_group_changed_event_is_received(TestTaskData(id = "some-id").asCandidateGroupChangedEvent("simpsons", CamundaTaskEvent.CANDIDATE_GROUP_ADD))
      .and()
      .time_passes_until_query_update_is_emitted()

    then()
      .task_has_candidate_groups("some-id", setOf("simpsons"))
      // TODO: How to get updates when a task _was_ but is no longer relevant for a query?
//      .and()
//      .query_updates_have_been_emitted(TasksForUserQuery(User("gonzo", setOf("muppetshow"))), testTaskData.asTask().copy(candidateGroups = setOf("simpsons")))
      .and()
      .query_updates_have_been_emitted(TasksForUserQuery(User("lisa", setOf("simpsons"))), testTaskData.asTask().copy(candidateGroups = setOf("simpsons")))
  }

  @Test
  fun `candidate users are updated`() {
    given()
      .no_task_exists()
      .and()
      .task_created_event_is_received(TestTaskData(id = "some-id", candidateUsers = setOf("kermit")).asTaskCreatedEngineEvent())

    `when`()
      .task_candidate_user_changed_event_is_received(TestTaskData(id = "some-id").asCandidateUserChangedEvent("kermit", CamundaTaskEvent.CANDIDATE_USER_DELETE))
      .and()
      .time_passes_until_query_update_is_emitted()
      .and()
      .task_candidate_user_changed_event_is_received(TestTaskData(id = "some-id").asCandidateUserChangedEvent("gonzo", CamundaTaskEvent.CANDIDATE_USER_ADD))
      .and()
      .time_passes_until_query_update_is_emitted()

    then()
      .task_has_candidate_users("some-id", setOf("gonzo"))
      // TODO: How to get updates when a task _was_ but is no longer relevant for a query?
//      .and()
//      .query_updates_have_been_emitted(TasksForUserQuery(kermit), TestTaskData(id = "some-id", candidateUsers = setOf("gonzo")).asTask())
      .and()
      .query_updates_have_been_emitted(TasksForUserQuery(User("gonzo", setOf("muppets"))), TestTaskData(id = "some-id", candidateUsers = setOf("gonzo")).asTask())
  }

  @Test
  fun `task is deleted`() {
    given()
      .no_task_exists()
      .and()
      .task_created_event_is_received(TestTaskData(id = "some-id").asTaskCreatedEngineEvent())

    `when`()
      .task_deleted_event_is_received(TestTaskData(id = "some-id").asTaskDeletedEngineEvent())
      .and()
      .time_passes_until_query_update_is_emitted()

    then()
      .task_does_not_exist("some-id")
      .and()
      .query_updates_have_been_emitted(TasksForUserQuery(User("kermit", setOf("muppetshow"))), TestTaskData(id = "some-id", deleted = true).asTask())
  }


  @Test
  fun `task is visible only for assigned user`() {
    given()
      .no_task_exists()

    `when`()
      .task_created_event_is_received(TestTaskData(id = "some-id", assignee = "kermit", candidateUsers = setOf()).asTaskCreatedEngineEvent())
      .and()
      .task_created_event_is_received(TestTaskData(id = "some-id-2", assignee = "kermit", candidateUsers = setOf()).asTaskCreatedEngineEvent())
      .and()
      .task_created_event_is_received(TestTaskData(id = "some-other-id", assignee = "piggy", candidateUsers = setOf()).asTaskCreatedEngineEvent())
      .and()
      .task_created_event_is_received(TestTaskData(id = "some-other-id-2", assignee = "piggy", candidateUsers = setOf()).asTaskCreatedEngineEvent())

    then()
      .tasks_with_payload_are_visible_to(User("kermit", setOf()), "some-id", "some-id-2")
      .and()
      .tasks_with_payload_are_visible_to(User("piggy", setOf()), "some-other-id", "some-other-id-2")

  }

  @Test
  fun `task is visible only for candidate user`() {
    given()
      .no_task_exists()

    `when`()
      .task_created_event_is_received(TestTaskData(id = "some-id", assignee = null, candidateUsers = setOf("kermit", "gonzo")).asTaskCreatedEngineEvent())
      .and()
      .task_created_event_is_received(TestTaskData(id = "some-id-2", assignee = null, candidateUsers = setOf("kermit")).asTaskCreatedEngineEvent())
      .and()
      .task_created_event_is_received(TestTaskData(id = "some-other-id", assignee = null, candidateUsers = setOf("piggy")).asTaskCreatedEngineEvent())
      .and()
      .task_created_event_is_received(TestTaskData(id = "some-other-id-2", assignee = null, candidateUsers = setOf("piggy")).asTaskCreatedEngineEvent())

    then()
      .tasks_with_payload_are_visible_to(User("kermit", setOf()), "some-id", "some-id-2")
      .and()
      .tasks_with_payload_are_visible_to(User("piggy", setOf()), "some-other-id", "some-other-id-2")

  }

  @Test
  fun `task is visible only for candidate group`() {
    given()
      .no_task_exists()

    `when`()
      .task_created_event_is_received(TestTaskData(id = "some-id", assignee = null, candidateUsers = setOf(), candidateGroups = setOf("one")).asTaskCreatedEngineEvent())
      .and()
      .task_created_event_is_received(TestTaskData(id = "some-id-2", assignee = null, candidateUsers = setOf(), candidateGroups = setOf("one")).asTaskCreatedEngineEvent())
      .and()
      .task_created_event_is_received(TestTaskData(id = "some-other-id", assignee = null, candidateUsers = setOf(), candidateGroups = setOf("two")).asTaskCreatedEngineEvent())
      .and()
      .task_created_event_is_received(TestTaskData(id = "some-other-id-2", assignee = null, candidateUsers = setOf(), candidateGroups = setOf("two", "baz")).asTaskCreatedEngineEvent())

    then()
      .tasks_with_payload_are_visible_to(User("kermit", setOf("one", "bar")), "some-id", "some-id-2")
      .and()
      .tasks_with_payload_are_visible_to(User("piggy", setOf("two", "zet")), "some-other-id", "some-other-id-2")

  }

  @Test
  fun `tasks are counted by application name`() {
    given()
      .no_task_exists()

    `when`()
      .task_created_event_is_received(TestTaskData(id = "task-1", sourceReference = processReference(applicationName = "app-1")).asTaskCreatedEngineEvent())
      .and()
      .time_passes_until_query_update_is_emitted(TaskCountByApplicationQuery::class.java)
      .and()
      .task_created_event_is_received(TestTaskData(id = "task-2", sourceReference = processReference(applicationName = "app-2")).asTaskCreatedEngineEvent())
      .and()
      .time_passes_until_query_update_is_emitted(TaskCountByApplicationQuery::class.java)
      .and()
      .task_created_event_is_received(TestTaskData(id = "task-3", sourceReference = processReference(applicationName = "app-1")).asTaskCreatedEngineEvent())
      .and()
      .time_passes_until_query_update_is_emitted(TaskCountByApplicationQuery::class.java)
      .and()
      .task_created_event_is_received(TestTaskData(id = "task-4", sourceReference = processReference(applicationName = "app-2")).asTaskCreatedEngineEvent())
      .and()
      .time_passes_until_query_update_is_emitted(TaskCountByApplicationQuery::class.java)
      .and()
      .task_deleted_event_is_received(TestTaskData(id = "task-4", sourceReference = processReference(applicationName = "app-2")).asTaskDeletedEngineEvent())
      .and()
      .time_passes_until_query_update_is_emitted(TaskCountByApplicationQuery::class.java)

    then()
      .task_counts_per_application_are(ApplicationWithTaskCount("app-1", 2), ApplicationWithTaskCount("app-2", 1))
      .and()
      .query_updates_have_been_emitted(
        TaskCountByApplicationQuery(),
        ApplicationWithTaskCount("app-1", 1),
        ApplicationWithTaskCount("app-2", 1),
        ApplicationWithTaskCount("app-1", 2),
        ApplicationWithTaskCount("app-2", 2),
        ApplicationWithTaskCount("app-2", 1)
      )
  }

  @Test
  fun `tasks are returned for application name`() {
    val task1 = TestTaskData(id = "task-1", sourceReference = processReference(applicationName = "app-1"))
    val task2 = TestTaskData(id = "task-2", sourceReference = processReference(applicationName = "app-2"))
    val task3 = TestTaskData(id = "task-3", sourceReference = processReference(applicationName = "app-1"))

    given()
      .no_task_exists()

    `when`()
      .task_created_event_is_received(task1.asTaskCreatedEngineEvent())
      .and()
      .task_created_event_is_received(task2.asTaskCreatedEngineEvent())
      .and()
      .task_created_event_is_received(task3.asTaskCreatedEngineEvent())

    then()
      .tasks_are_returned_for_application("app-1", TaskQueryResult(listOf(task1.asTask(), task3.asTask())))
  }

}

@TestPropertySource(properties = [
  "camunda.taskpool.view.mongo.changeTrackingMode=CHANGE_STREAM",
  "spring.data.mongodb.database=TaskPoolMongoServiceChangeStreamChangeTrackingITest"
])
@ActiveProfiles("itest-replicated")
class TaskPoolMongoServiceChangeStreamChangeTrackingITest : TaskPoolMongoServiceITestBase() {
  companion object {
    private val mongo = MongoLauncher.MongoInstance(true, "TaskPoolMongoServiceChangeStreamChangeTrackingITest")

    @BeforeClass
    @JvmStatic
    fun initMongo() {
      mongo.init()
    }

    @AfterClass
    @JvmStatic
    fun stop() {
      mongo.stop()
    }
  }

  @After
  fun clearMongo() {
    mongo.clear()
  }
}

@TestPropertySource(properties = [
  "camunda.taskpool.view.mongo.changeTrackingMode=EVENT_HANDLER",
  "spring.data.mongodb.database=TaskPoolMongoServiceEventHandlerChangeTrackingITest"
])
@ActiveProfiles("itest-standalone")
class TaskPoolMongoServiceEventHandlerChangeTrackingITest : TaskPoolMongoServiceITestBase() {
  companion object {
    private val mongo = MongoLauncher.MongoInstance(false, "TaskPoolMongoServiceEventHandlerChangeTrackingITest")

    @BeforeClass
    @JvmStatic
    fun initMongo() {
      mongo.init()
    }

    @AfterClass
    @JvmStatic
    fun stop() {
      mongo.stop()
    }
  }

  @After
  fun clearMongo() {
    mongo.clear()
  }
}


data class TestTaskData(
  val id: String,
  val sourceReference: SourceReference = processReference(),
  val taskDefinitionKey: String = "task-definition-key-abcde",
  val payload: VariableMap = Variables.fromMap(mapOf(Pair("variableKey", "variableValue"))),
  val correlations: CorrelationMap = Variables.fromMap(mapOf(Pair("correlationKey", "correlationValue"))),
  val businessKey: String? = "businessKey",
  val name: String? = "task-name",
  val description: String? = "some task description",
  val formKey: String? = "app:form-key",
  val priority: Int? = 0,
  val createTime: Date? = Date(1234567890L),
  val candidateUsers: Set<String> = setOf("kermit", "piggy"),
  val candidateGroups: Set<String> = setOf("muppetshow"),
  val assignee: String? = null,
  val owner: String? = null,
  val dueDate: Date? = Date(1234599999L),
  val followUpDate: Date? = null,
  val deleted: Boolean = false) {

  fun asTaskCreatedEngineEvent() = TaskCreatedEngineEvent(
    id = id,
    sourceReference = sourceReference,
    taskDefinitionKey = taskDefinitionKey,
    payload = payload,
    correlations = correlations,
    businessKey = businessKey,
    name = name,
    description = description,
    formKey = formKey,
    priority = priority,
    createTime = createTime,
    candidateUsers = candidateUsers,
    candidateGroups = candidateGroups,
    assignee = assignee,
    owner = owner,
    dueDate = dueDate,
    followUpDate = followUpDate
  )

  fun asTaskAssignedEngineEvent() = TaskAssignedEngineEvent(
    id = id,
    sourceReference = sourceReference,
    taskDefinitionKey = taskDefinitionKey,
    payload = payload,
    correlations = correlations,
    businessKey = businessKey,
    name = name,
    description = description,
    formKey = formKey,
    priority = priority,
    createTime = createTime,
    candidateUsers = candidateUsers,
    candidateGroups = candidateGroups,
    assignee = assignee,
    owner = owner,
    dueDate = dueDate,
    followUpDate = followUpDate
  )

  fun asTaskAttributeUpdatedEvent() = TaskAttributeUpdatedEngineEvent(
    id = id,
    sourceReference = sourceReference,
    taskDefinitionKey = taskDefinitionKey,
    name = name,
    description = description,
    priority = priority,
    owner = owner,
    dueDate = dueDate,
    followUpDate = followUpDate
  )

  fun asCandidateGroupChangedEvent(groupId: String, assignmentUpdateType: String) = TaskCandidateGroupChanged(
    id = id,
    sourceReference = sourceReference,
    taskDefinitionKey = taskDefinitionKey,
    groupId = groupId,
    assignmentUpdateType = assignmentUpdateType
  )

  fun asCandidateUserChangedEvent(userId: String, assignmentUpdateType: String) = TaskCandidateUserChanged(
    id = id,
    sourceReference = sourceReference,
    taskDefinitionKey = taskDefinitionKey,
    userId = userId,
    assignmentUpdateType = assignmentUpdateType
  )

  fun asTaskDeletedEngineEvent() = TaskDeletedEngineEvent(
    id = id,
    sourceReference = sourceReference,
    taskDefinitionKey = taskDefinitionKey,
    payload = payload,
    correlations = correlations,
    businessKey = businessKey,
    name = name,
    description = description,
    formKey = formKey,
    priority = priority,
    createTime = createTime,
    candidateUsers = candidateUsers,
    candidateGroups = candidateGroups,
    assignee = assignee,
    owner = owner,
    dueDate = dueDate,
    followUpDate = followUpDate,
    deleteReason = "For testing purposes"
  )

  fun asTask() = Task(
    id = id,
    sourceReference = sourceReference,
    taskDefinitionKey = taskDefinitionKey,
    payload = payload,
    correlations = correlations,
    businessKey = businessKey,
    name = name,
    description = description,
    formKey = formKey,
    priority = priority,
    createTime = createTime,
    candidateUsers = candidateUsers,
    candidateGroups = candidateGroups,
    assignee = assignee,
    owner = owner,
    dueDate = dueDate,
    followUpDate = followUpDate,
    deleted = deleted
  )
}

private fun processReference(instanceId: String = "instance-id-12345",
                             executionId: String = "execution-id-12345",
                             definitionId: String = "definition-id-12345",
                             definitionKey: String = "definition-key-abcde",
                             name: String = "process-name",
                             applicationName: String = "application-name"): ProcessReference {
  return ProcessReference(
    instanceId,
    executionId,
    definitionId,
    definitionKey,
    name,
    applicationName
  )
}

private fun Task.withDataEntries(dataEntries: List<DataEntry> = listOf()) = TaskWithDataEntries(this, dataEntries)
