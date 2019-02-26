package io.holunda.camunda.taskpool.view.mongo.service

import com.tngtech.jgiven.integration.spring.SpringRuleScenarioTest
import io.holunda.camunda.taskpool.api.business.CorrelationMap
import io.holunda.camunda.taskpool.api.task.*
import io.holunda.camunda.taskpool.view.Task
import io.holunda.camunda.taskpool.view.mongo.TaskpoolMongoTestApplication
import org.camunda.bpm.engine.variable.VariableMap
import org.camunda.bpm.engine.variable.Variables
import org.junit.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import java.util.*

@SpringBootTest(classes = [TaskpoolMongoTestApplication::class])
@ActiveProfiles("mongo")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class TaskPoolMongoServiceITest : SpringRuleScenarioTest<TaskPoolGivenStage<*>, TaskPoolWhenStage<*>, TaskPoolThenStage<*>>() {

  @Test
  fun `a task is created on receiving TaskCreatedEngineEvent`() {

    val testData = TestTaskData(id = "some-id", assignee = "kermit")
    val expected = testData.asTask()

    given()
      .no_task_exists()

    `when`()
      .task_created_event_is_received(testData.asTaskCreatedEngineEvent())
    then()
      .task_is_created(expected)
      .and()
      .tasks_visible_to_assignee_or_candidate_user("kermit", listOf(expected))
      .and()
      .tasks_visible_to_assignee_or_candidate_user("piggy", listOf(expected))
      .and()
      .tasks_visible_to_candidate_group("muppetshow", listOf(expected))
  }

  @Test
  fun `a task is assigned on receiving TaskAssignedEngineEvent`() {
    given()
      .no_task_exists()
      .and()
      .task_created_event_is_received(TestTaskData(id = "some-id").asTaskCreatedEngineEvent())

    `when`()
      .task_assign_event_is_received(TestTaskData(id = "some-id", assignee = "kermit").asTaskAssignedEngineEvent())

    then()
      .task_is_assigned_to("some-id", "kermit")
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
  }

  @Test
  fun `candidate groups are updated`() {
    given()
      .no_task_exists()
      .and()
      .task_created_event_is_received(TestTaskData(id = "some-id", candidateGroups = setOf("muppetshow")).asTaskCreatedEngineEvent())

    `when`()
      .task_candidate_group_changed_event_is_received(TestTaskData(id = "some-id").asCandidateGroupChangedEvent("muppetshow", CamundaTaskEvent.CANDIDATE_GROUP_DELETE))
      .and()
      .task_candidate_group_changed_event_is_received(TestTaskData(id = "some-id").asCandidateGroupChangedEvent("simpsons", CamundaTaskEvent.CANDIDATE_GROUP_ADD))

    then()
      .task_has_candidate_groups("some-id", setOf("simpsons"))
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
      .task_candidate_user_changed_event_is_received(TestTaskData(id = "some-id").asCandidateUserChangedEvent("gonzo", CamundaTaskEvent.CANDIDATE_USER_ADD))

    then()
      .task_has_candidate_users("some-id", setOf("gonzo"))
  }

}

data class TestTaskData(
  val id: String,
  val sourceReference: SourceReference = ProcessReference(
    "instance-id-12345",
    "execution-id-12345",
    "definition-id-12345",
    "definition-key-abcde",
    "process-name",
    "application-name"
  ),
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
  val followUpDate: Date? = null) {

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
    assignee = assignee)

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
    followUpDate = followUpDate
  )
}
