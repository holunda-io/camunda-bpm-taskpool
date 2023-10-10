package io.holunda.polyflow.view.simple.service

import com.tngtech.jgiven.junit5.ScenarioTest
import io.holunda.camunda.taskpool.api.task.CamundaTaskEventType
import io.holunda.camunda.taskpool.api.task.ProcessReference
import io.holunda.polyflow.view.query.task.ApplicationWithTaskCount
import org.camunda.bpm.engine.variable.Variables
import org.junit.jupiter.api.Test
import java.util.*

class SimpleTaskPoolServiceTest : ScenarioTest<SimpleTaskPoolGivenStage<*>, SimpleTaskPoolWhenStage<*>, SimpleTaskPoolThenStage<*>>() {

  @Test
  fun `slice contains complete list if input list is smaller than desired page size`() {
    given()
      .tasks_exist(13)

    `when`()
      .page_is_queried(0, 83)

    then()
      .num_tasks_are_returned(13)
  }

  @Test
  fun `slice for page 0 contains the desired count of elements`() {
    given()
      .tasks_exist(111)

    `when`()
      .page_is_queried(0, 83)

    then()
      .num_tasks_are_returned(83)
  }

  @Test
  fun `slice for page 1 contains elements from after page 0 to end of list`() {
    given()
      .tasks_exist(111)

    `when`()
      .page_is_queried(1, 83)

    then()
      .num_tasks_are_returned(111 - 83)
  }

  @Test
  fun `paging returns each element exactly once`() {
    given()
      .tasks_exist(111)

    `when`()
      .page_is_queried(0, 83)
      .and()
      .page_is_queried(1, 83)

    then()
      .all_tasks_are_returned()
  }

  @Test
  fun `a task is created on receiving TaskCreatedEngineEvent`() {
    given()
      .no_task_exists()

    `when`()
      .task_created_event_is_received(TestTaskData(id = "some-id").asTaskCreatedEngineEvent())

    then()
      .task_is_created(TestTaskData(id = "some-id").asTask())
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
  fun `a non existing task is not assigned`() {
    given()
      .no_task_exists()

    `when`()
      .task_assign_event_is_received(TestTaskData(id = "some-id", assignee = "kermit").asTaskAssignedEngineEvent())

    then()
      .task_does_not_exist("some-id")
      .and()
      .task_is_not_found_for_user("kermit", "some-id")
  }

  @Test
  fun `candidate groups are updated`() {
    given()
      .no_task_exists()
      .and()
      .task_created_event_is_received(TestTaskData(id = "some-id", candidateGroups = setOf("muppetshow")).asTaskCreatedEngineEvent())

    `when`()
      .task_candidate_group_changed_event_is_received(TestTaskData(id = "some-id").asCandidateGroupChangedEvent("muppetshow", CamundaTaskEventType.CANDIDATE_GROUP_DELETE))
      .and()
      .task_candidate_group_changed_event_is_received(TestTaskData(id = "some-id").asCandidateGroupChangedEvent("simpsons", CamundaTaskEventType.CANDIDATE_GROUP_ADD))

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
      .task_candidate_user_changed_event_is_received(TestTaskData(id = "some-id").asCandidateUserChangedEvent("kermit", CamundaTaskEventType.CANDIDATE_USER_DELETE))
      .and()
      .task_candidate_user_changed_event_is_received(TestTaskData(id = "some-id").asCandidateUserChangedEvent("gonzo", CamundaTaskEventType.CANDIDATE_USER_ADD))

    then()
      .task_has_candidate_users("some-id", setOf("gonzo"))
  }

  @Test
  fun `tasks are counted by application name`() {
    given()
      .tasks_exist_from_application(5, "app-1")
      .and()
      .tasks_exist_from_application(42, "app-2")

    `when`()
      .task_count_queried()

    then()
      .task_counts_are("app-1" withTaskCount 5, "app-2" withTaskCount 42)
  }

  @Test
  fun `tasks are returned for application name`() {
    val task1 = TestTaskData(id = "task-1", sourceReference = processReference(applicationName = "app-1"))
    val task2 = TestTaskData(id = "task-2", sourceReference = processReference(applicationName = "app-2"))
    val task3 = TestTaskData(id = "task-3", sourceReference = processReference(applicationName = "app-1"))

    given()
      .no_task_exists()
      .and()
      .task_created_event_is_received(task1.asTaskCreatedEngineEvent())
      .and()
      .task_created_event_is_received(task2.asTaskCreatedEngineEvent())
      .and()
      .task_created_event_is_received(task3.asTaskCreatedEngineEvent())

    `when`()
      .tasks_queried_for_application("app-1")

    then()
      .tasks_are_returned_for_application(task1.asTask(), task3.asTask())
  }


  @Test
  fun `retrieves only tasks smaller than payload int value`() {
    given()
      .tasks_exist(13)

    `when`()
      .tasks_are_queried(listOf("payloadIdInt<5"))

    then()
      .tasks_are_returned(
        then().tasks.take(5)
      )
  }

  @Test
  fun `retrieves only tasks with given task definition key`() {
    given()
      .tasks_exist(13)

    `when`()
      .tasks_are_queried(listOf("task.taskDefinitionKey=task-key-3", "payloadIdInt<5"))

    then()
      .tasks_are_returned(
        then().tasks.subList(3, 4) // only the third, because the task definition key is limiting from 5 to 1...
      )
  }


  @Test
  fun `retrieves only fifth by payload string value`() {
    given()
      .tasks_exist(13)

    `when`()
      .tasks_are_queried(listOf("payloadIdString=5"))

    then()
      .tasks_are_returned(listOf(then().tasks[5]))
  }

  @Test
  fun `retrieves only fifth by task attribute`() {
    given()
      .tasks_exist(13)

    `when`()
      .tasks_are_queried(listOf("task.id=id5"))

    then()
      .tasks_are_returned(listOf(then().tasks[5]))
  }

  @Test
  fun `retrieves tasks by like search`() {
    given()
      .tasks_exist(13)

    `when`()
      .tasks_are_queried(listOf("task.id%id5"))

    then()
      .tasks_are_returned(listOf(then().tasks[5]))
  }

  @Test
  fun `retrieves all tasks by like search`() {
    given()
      .tasks_exist(13)

    `when`()
      .all_tasks_are_queried(listOf("task.id%id5"))

    then()
      .tasks_are_returned(listOf(then().tasks[5]))
  }

  @Test
  fun `retrieves all task sort by multiple values`() {
    given()
      .tasks_exist(5)

    `when`()
      .all_tasks_are_queried(filters = listOf(), sort = listOf("+task.dueDate", "-task.businessKey"))

    then()
      .all_task_are_returned_and_sorted_by(reversed = true) { it.task.businessKey }
  }

  private infix fun String.withTaskCount(taskCount: Int) = ApplicationWithTaskCount(this, taskCount)
}

private fun processReference(
  instanceId: String = "instance-id-12345",
  executionId: String = "execution-id-12345",
  definitionId: String = "definition-id-12345",
  definitionKey: String = "definition-key-abcde",
  name: String = "process-name",
  applicationName: String = "application-name"
): ProcessReference {
  return ProcessReference(
    instanceId,
    executionId,
    definitionId,
    definitionKey,
    name,
    applicationName
  )
}
