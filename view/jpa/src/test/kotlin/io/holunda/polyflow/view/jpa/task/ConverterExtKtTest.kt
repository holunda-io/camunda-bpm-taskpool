package io.holunda.polyflow.view.jpa.task

import com.fasterxml.jackson.databind.ObjectMapper
import io.holunda.camunda.taskpool.api.task.ProcessReference
import io.holunda.camunda.taskpool.api.task.TaskAttributeUpdatedEngineEvent
import io.holunda.polyflow.view.jpa.data.DataEntryId
import io.holunda.polyflow.view.jpa.payload.PayloadAttribute
import io.holunda.polyflow.view.jpa.process.toSourceReferenceEmbeddable
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.data.MapEntry
import org.camunda.bpm.engine.variable.Variables
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.Date

class ConverterExtKtTest {

  @Test
  fun `should convert to task`() {
    val objectMapper = ObjectMapper()
    val entity = TaskEntity(
      taskId = "taskId",
      taskDefinitionKey = "taskDefinitionKey",
      name = "name",
      priority = 50,
      sourceReference = ProcessReference(
        instanceId = "instanceId",
        executionId = "executionId",
        definitionId = "definitionId",
        definitionKey = "definitionKey",
        name = "name",
        applicationName = "applicationName",
      ).toSourceReferenceEmbeddable(),
      authorizedPrincipals = mutableSetOf("GROUP:FOO", "GROUP:BAR"),
      correlations = mutableSetOf(DataEntryId("1", "foo"), DataEntryId("2", "bar")),
      payloadAttributes = mutableSetOf(PayloadAttribute("foo", "bar")),
      payload = """
        {"foo": "bar" }
      """.trimIndent(),
      formKey = "formKey",
      )

    val task = entity.toTask(objectMapper)

    assertThat(task.id).isEqualTo("taskId")
    assertThat(task.taskDefinitionKey).isEqualTo("taskDefinitionKey")
    assertThat(task.name).isEqualTo("name")
    assertThat(task.priority).isEqualTo(50)
    assertThat(task.sourceReference.instanceId).isEqualTo("instanceId")
    assertThat(task.sourceReference.executionId).isEqualTo("executionId")
    assertThat(task.sourceReference.definitionId).isEqualTo("definitionId")
    assertThat(task.sourceReference.definitionKey).isEqualTo("definitionKey")
    assertThat(task.sourceReference.name).isEqualTo("name")
    assertThat(task.sourceReference.applicationName).isEqualTo("applicationName")
    assertThat(task.candidateGroups).containsExactlyInAnyOrder("FOO", "BAR")
    assertThat(task.correlations).containsOnly(MapEntry.entry("bar", "2"), MapEntry.entry("foo", "1"))
    assertThat(task.payload).containsOnly(MapEntry.entry("foo", "bar"))
    assertThat(task.formKey).isEqualTo("formKey")

  }

  @Test
  fun `should process update`() {
    val objectMapper = ObjectMapper()
    val entity = TaskEntity(
      taskId = "taskId",
      taskDefinitionKey = "taskDefinitionKey",
      name = "name",
      priority = 50,
      sourceReference = ProcessReference(
        instanceId = "instanceId",
        executionId = "executionId",
        definitionId = "definitionId",
        definitionKey = "definitionKey",
        name = "name",
        applicationName = "applicationName",
      ).toSourceReferenceEmbeddable(),
      authorizedPrincipals = mutableSetOf("GROUP:FOO", "GROUP:BAR"),
      correlations = mutableSetOf(DataEntryId("1", "foo"), DataEntryId("2", "bar")),
      payloadAttributes = mutableSetOf(PayloadAttribute("foo", "bar")),
      payload = """
        {"foo": "bar" }
      """.trimIndent(),
      formKey = "formKey",
    )

    val followUpDate = Date.from( Instant.now())
    val taskAttributeUpdatedEngineEvent = TaskAttributeUpdatedEngineEvent(
      id = "taskId",
      taskDefinitionKey = "taskDefinitionKey",
      name = "updated name",
      priority = 10,
      sourceReference = ProcessReference(
        instanceId = "instanceId",
        executionId = "executionId",
        definitionId = "definitionId",
        definitionKey = "definitionKey",
        name = "name",
        applicationName = "updatedName",
      ),
      payload = Variables.createVariables().putValue("foo", "updated bar"),
      formKey = "updated formKey",
      followUpDate = followUpDate
    )

    entity.update(taskAttributeUpdatedEngineEvent, objectMapper, 10, listOf(), 10)

    assertThat(entity.name).isEqualTo("updated name")
    assertThat(entity.priority).isEqualTo(10)
    assertThat(entity.sourceReference.applicationName).isEqualTo("updatedName")
    assertThat(entity.payload).isEqualTo("""{"foo":"updated bar"}""")
    assertThat(entity.formKey).isEqualTo("updated formKey")
    assertThat(entity.followUpDate).isEqualTo(followUpDate.toInstant())
  }

  @Test
  fun `should process update with nulls`() {
    val objectMapper = ObjectMapper()
    val entity = TaskEntity(
      taskId = "taskId",
      taskDefinitionKey = "taskDefinitionKey",
      name = "name",
      priority = 50,
      sourceReference = ProcessReference(
        instanceId = "instanceId",
        executionId = "executionId",
        definitionId = "definitionId",
        definitionKey = "definitionKey",
        name = "name",
        applicationName = "applicationName",
      ).toSourceReferenceEmbeddable(),
      authorizedPrincipals = mutableSetOf("GROUP:FOO", "GROUP:BAR"),
      correlations = mutableSetOf(DataEntryId("1", "foo"), DataEntryId("2", "bar")),
      payloadAttributes = mutableSetOf(PayloadAttribute("foo", "bar")),
      payload = """
        {"foo": "bar" }
      """.trimIndent(),
      formKey = "formKey",
    )

    val taskAttributeUpdatedEngineEvent = TaskAttributeUpdatedEngineEvent(
      id = "taskId",
      taskDefinitionKey = "taskDefinitionKey",
      name = "updated name",
      priority = 10,
      sourceReference = ProcessReference(
        instanceId = "instanceId",
        executionId = "executionId",
        definitionId = "definitionId",
        definitionKey = "definitionKey",
        name = "name",
        applicationName = "updatedName",
      ),
      payload = Variables.createVariables().putValue("foo", "updated bar"),
      formKey = "updated formKey",
      followUpDate = null
    )

    entity.update(taskAttributeUpdatedEngineEvent, objectMapper, 10, listOf(), 10)

    assertThat(entity.name).isEqualTo("updated name")
    assertThat(entity.priority).isEqualTo(10)
    assertThat(entity.sourceReference.applicationName).isEqualTo("updatedName")
    assertThat(entity.payload).isEqualTo("""{"foo":"updated bar"}""")
    assertThat(entity.formKey).isEqualTo("updated formKey")
    assertThat(entity.followUpDate).isNull()
  }
}
