package io.holunda.camunda.taskpool.view.simple.service

import com.tngtech.jgiven.junit.ScenarioTest
import io.holunda.camunda.taskpool.api.business.newCorrelations
import io.holunda.camunda.taskpool.api.task.ProcessReference
import io.holunda.camunda.taskpool.api.task.TaskAssignedEngineEvent
import io.holunda.camunda.taskpool.api.task.TaskCreatedEngineEvent
import io.holunda.camunda.taskpool.view.Task
import org.camunda.bpm.engine.variable.Variables
import org.junit.Test
import java.util.*

class TaskPoolServiceTest : ScenarioTest<TaskPoolGivenStage<*>, TaskPoolWhenStage<*>, TaskPoolThenStage<*>>() {

  @Test
  fun `slice contains complete list if input list is smaller than desired page size`() {
    given()
      .tasks_exist(13)

    `when`()
      .tasks_queried(0, 83)

    then()
      .num_tasks_are_returned(13)
  }

  @Test
  fun `slice for page 0 contains the desired count of elements`() {
    given()
      .tasks_exist(111)

    `when`()
      .tasks_queried(0, 83)

    then()
      .num_tasks_are_returned(83)
  }

  @Test
  fun `slice for page 1 contains elements from after page 0 to end of list`() {
    given()
      .tasks_exist(111)

    `when`()
      .tasks_queried(1, 83)

    then()
      .num_tasks_are_returned(111 - 83)
  }

  @Test
  fun `paging returns each element exactly once`() {
    given()
      .tasks_exist(111)

    `when`()
      .tasks_queried(0, 83)
      .and()
      .tasks_queried(1, 83)

    then()
      .all_tasks_are_returned()
  }

  @Test
  fun `a task is created on receiving TaskCreatedEngineEvent`() {
    given()
      .no_task_exists()

    `when`()
      .task_created_event_is_received(makeTaskCreatedEvent())

    then()
      .task_is_created(makeTask())
  }

  @Test
  fun `a task is assigned on receiving TaskAssignedEngineEvent`() {
    given()
      .no_task_exists()
      .and()
      .task_created_event_is_received(makeTaskCreatedEvent())

    `when`()
      .task_assign_event_is_received(makeTaskAssignedEvent())

    then()
      .task_is_assigned_to("some-id", "kermit")
  }

  @Test
  fun `do not lose task task data by task assignment`() {
    given()
      .no_task_exists()
      .and()
      .task_created_event_is_received(makeTaskCreatedEvent())

    `when`()
      .task_assign_event_is_received(makeTaskAssignedEvent())

    then()
      .task_payload_matches("some-id", Variables.fromMap(mapOf(Pair("variableKey", "variableValue"))))
      .and()
      .task_correlations_match("some-id", Variables.fromMap(mapOf(Pair("correlationKey", "correlationValue"))))
  }

  @Test
  fun `as nonexisting task is not assigned`() {
    given()
      .no_task_exists()

    `when`()
      .task_assign_event_is_received(makeTaskAssignedEvent())

    then()
      .task_is_assigned_to("some-id", null)
  }

  private fun makeTaskCreatedEvent(): TaskCreatedEngineEvent =
    TaskCreatedEngineEvent(
      id = "some-id",
      sourceReference = ProcessReference(
        "instance-id-12345",
        "execution-id-12345",
        "definition-id-12345",
        "definition-key-abcde",
        "process-name",
        "application-name"
      ),
      taskDefinitionKey = "task-definition-key-abcde",
      payload = Variables.fromMap(mapOf(Pair("variableKey", "variableValue"))),
      correlations = Variables.fromMap(mapOf(Pair("correlationKey", "correlationValue"))),
      businessKey = "businessKey",
      name = "task-name",
      description = "some task description",
      formKey = "app:form-key",
      priority = 0,
      createTime = Date(1234567890L),
      candidateGroups = setOf("muppetshow"),
      candidateUsers = setOf("kermit", "piggy"),
      assignee = null,
      owner = null,
      dueDate = Date(1234599999L),
      followUpDate = null
    )

  private fun makeTaskAssignedEvent(): TaskAssignedEngineEvent =
    TaskAssignedEngineEvent(
      id = "some-id",
      sourceReference = ProcessReference(
        "instance-id-12345",
        "execution-id-12345",
        "definition-id-12345",
        "definition-key-abcde",
        "process-name",
        "application-name"
      ),
      taskDefinitionKey = "task-definition-key-abcde",
      payload = Variables.createVariables(),
      correlations = newCorrelations(),
      assignee = "kermit"
    )

  private fun makeTask(assignee: String? = null): Task =
    Task(
      id = "some-id",
      sourceReference = ProcessReference(
        "instance-id-12345",
        "execution-id-12345",
        "definition-id-12345",
        "definition-key-abcde",
        "process-name",
        "application-name"
      ),
      taskDefinitionKey = "task-definition-key-abcde",
      payload = Variables.fromMap(mapOf(Pair("variableKey", "variableValue"))),
      correlations = Variables.fromMap(mapOf(Pair("correlationKey", "correlationValue"))),
      businessKey = "businessKey",
      name = "task-name",
      description = "some task description",
      formKey = "app:form-key",
      priority = 0,
      createTime = Date(1234567890L),
      candidateGroups = setOf("muppetshow"),
      candidateUsers = setOf("kermit", "piggy"),
      assignee = assignee,
      owner = null,
      dueDate = Date(1234599999L),
      followUpDate = null
    )

}
