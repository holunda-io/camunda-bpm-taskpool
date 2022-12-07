package io.holunda.polyflow.taskpool.sender.task.accumulator

import io.holunda.camunda.taskpool.api.task.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.util.*

internal class EngineTaskCommandIntentDetectorTest {

  private val detector: EngineTaskCommandIntentDetector = SimpleEngineTaskCommandIntentDetector(false)

  @Test
  fun `detects single create`() {
    val intent = detector.detectIntents(
      listOf(
        assign(),
        create(),
        update(),
        update()
      )
    )

    assertThat(intent).hasSize(1)
    assertThat(intent[0]).hasSize(4)
    assertThat(intent[0].first()).isEqualTo(create())
  }

  @Test
  fun `detects single update`() {
    val intent = detector.detectIntents(
      listOf(
        update(),
        update(),
        update(),
        update(),
      )
    )

    assertThat(intent).hasSize(1)
    assertThat(intent[0]).hasSize(4)
    assertThat(intent[0].first()).isEqualTo(update())
  }

  @Test
  fun `detects two update and assign`() {
    val intent = detector.detectIntents(
      listOf(
        assign(),
        update(),
        update(),
        update(),
      )
    )

    assertThat(intent).hasSize(2)
    assertThat(intent[0]).hasSize(1)
    assertThat(intent[0].first()).isEqualTo(assign())
    assertThat(intent[1]).hasSize(3)
    assertThat(intent[1].first()).isEqualTo(update())
  }


  @Test
  fun `detects single candidate group change`() {
    val intent = detector.detectIntents(
      listOf(
        addGroups(),
        addGroups(),
      )
    )

    assertThat(intent).hasSize(1)
    assertThat(intent[0]).hasSize(2)
    assertThat(intent[0].first()).isEqualTo(addGroups())
  }

  @Test
  fun `detects single candidate users change`() {
    val intent = detector.detectIntents(
      listOf(
        addUsers(),
        addUsers(),
      )
    )

    assertThat(intent).hasSize(1)
    assertThat(intent[0]).hasSize(2)
    assertThat(intent[0].first()).isEqualTo(addUsers())
  }

  @Test
  fun `detects two candidate users and group change`() {
    val intent = detector.detectIntents(
      listOf(
        addUsers(),
        addGroups(),
      )
    )

    assertThat(intent).hasSize(2)
    assertThat(intent[0]).hasSize(1)
    assertThat(intent[0].first()).isEqualTo(addUsers())
    assertThat(intent[1]).hasSize(1)
    assertThat(intent[1].first()).isEqualTo(addGroups())
  }

  @Test
  fun `detects two candidate groups changes`() {
    val intent = detector.detectIntents(
      listOf(
        addGroups(),
        deleteGroups(),
      )
    )

    assertThat(intent).hasSize(2)
    assertThat(intent[0]).hasSize(1)
    assertThat(intent[0].first()).isEqualTo(addGroups())
    assertThat(intent[1]).hasSize(1)
    assertThat(intent[1].first()).isEqualTo(deleteGroups())
  }

  @Test
  fun `detects two candidate users changes`() {
    val intent = detector.detectIntents(
      listOf(
        addUsers(),
        deleteUsers(),
      )
    )

    assertThat(intent).hasSize(2)
    assertThat(intent[0]).hasSize(1)
    assertThat(intent[0].first()).isEqualTo(addUsers())
    assertThat(intent[1]).hasSize(1)
    assertThat(intent[1].first()).isEqualTo(deleteUsers())
  }


  @Test
  fun `detects complete with assign`() {
    val intent = detector.detectIntents(
      listOf(
        addUsers(),
        deleteUsers(),
        assign(),
        complete()
      )
    )

    assertThat(intent).hasSize(1)
    assertThat(intent[0]).hasSize(4)
    assertThat(intent[0].first()).isEqualTo(complete())
  }



  private fun create() = CreateTaskCommand(
    id = "",
    sourceReference = makeProcessReference(),
    taskDefinitionKey = "key"
  )

  private fun assign() = AssignTaskCommand(
    id = "",
    assignee = null
  )

  private fun complete() = CompleteTaskCommand(
    id = "",
  )

  private fun update() = UpdateAttributeTaskCommand(
    id = "",
    sourceReference = makeProcessReference(),
    taskDefinitionKey = "key",
    name = null,
    description = null,
    owner = null,
    priority = null
  )

  private fun delete() = DeleteTaskCommand(
    id = "",
    deleteReason = "deleted"
  )

  private fun addGroups() = AddCandidateGroupsCommand(
    id = "",
    candidateGroups = setOf()
  )

  private fun deleteGroups() = DeleteCandidateGroupsCommand(
    id = "",
    candidateGroups = setOf()
  )

  private fun addUsers() = AddCandidateUsersCommand(
    id = "",
    candidateUsers = setOf()
  )

  private fun deleteUsers() = DeleteCandidateUsersCommand(
    id = "",
    candidateUsers = setOf()
  )


  private fun taskId() = UUID.randomUUID().toString()

  private fun makeProcessReference() = ProcessReference(
    "instance-id-12345",
    "execution-id-12345",
    "definition-id-12345",
    "definition-key-abcde",
    "process-name",
    "application-name"
  )

}