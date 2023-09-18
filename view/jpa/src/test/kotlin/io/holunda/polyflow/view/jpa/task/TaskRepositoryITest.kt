package io.holunda.polyflow.view.jpa.task

import com.fasterxml.jackson.databind.ObjectMapper
import io.holunda.camunda.variable.serializer.toJsonPathsWithValues
import io.holunda.camunda.variable.serializer.toPayloadJson
import io.holunda.polyflow.view.jpa.DbCleaner
import io.holunda.polyflow.view.jpa.auth.AuthorizationPrincipal.Companion.group
import io.holunda.polyflow.view.jpa.auth.AuthorizationPrincipal.Companion.user
import io.holunda.polyflow.view.jpa.data.DataEntryId
import io.holunda.polyflow.view.jpa.emptyTask
import io.holunda.polyflow.view.jpa.itest.TestApplicationDataJpa
import io.holunda.polyflow.view.jpa.payload.PayloadAttribute
import io.holunda.polyflow.view.jpa.task.TaskRepository.Companion.hasApplication
import io.holunda.polyflow.view.jpa.task.TaskRepository.Companion.hasBusinessKey
import io.holunda.polyflow.view.jpa.task.TaskRepository.Companion.hasDueDate
import io.holunda.polyflow.view.jpa.task.TaskRepository.Companion.hasDueDateAfter
import io.holunda.polyflow.view.jpa.task.TaskRepository.Companion.hasDueDateBefore
import io.holunda.polyflow.view.jpa.task.TaskRepository.Companion.hasFollowUpDate
import io.holunda.polyflow.view.jpa.task.TaskRepository.Companion.hasFollowUpDateAfter
import io.holunda.polyflow.view.jpa.task.TaskRepository.Companion.hasFollowUpDateBefore
import io.holunda.polyflow.view.jpa.task.TaskRepository.Companion.hasProcessName
import io.holunda.polyflow.view.jpa.task.TaskRepository.Companion.hasTaskPayloadAttribute
import io.holunda.polyflow.view.jpa.task.TaskRepository.Companion.isAuthorizedFor
import io.holunda.polyflow.view.jpa.task.TaskRepository.Companion.likeBusinessKey
import io.holunda.polyflow.view.jpa.task.TaskRepository.Companion.likeDescription
import io.holunda.polyflow.view.jpa.task.TaskRepository.Companion.likeName
import io.holunda.polyflow.view.jpa.task.TaskRepository.Companion.likeProcessName
import io.holunda.polyflow.view.jpa.task.TaskRepository.Companion.likeTextSearch
import jakarta.persistence.EntityManager
import org.assertj.core.api.Assertions.assertThat
import org.camunda.bpm.engine.variable.Variables.createVariables
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.*

@DataJpaTest(showSql = false)
@ContextConfiguration(classes = [TestApplicationDataJpa::class])
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("itest", "mock-query-emitter")
class TaskRepositoryITest {
  @Autowired
  lateinit var entityManager: EntityManager

  @Autowired
  lateinit var taskRepository: TaskRepository

  @Autowired
  lateinit var dbCleaner: DbCleaner

  @Autowired
  lateinit var objectMapper: ObjectMapper

  lateinit var task1: TaskEntity
  lateinit var task2: TaskEntity

  private val today = LocalDate.now().atStartOfDay().toInstant(ZoneOffset.UTC)
  private val tomorrow = LocalDate.now().atStartOfDay().plusDays(1).toInstant(ZoneOffset.UTC)
  private val dayAfterTomorrow = LocalDate.now().atStartOfDay().plusDays(2).toInstant(ZoneOffset.UTC)
  private val twoDaysAfterTomorrow = LocalDate.now().atStartOfDay().plusDays(3).toInstant(ZoneOffset.UTC)

  @BeforeEach
  fun `insert entries`() {

    val payload = createVariables().apply {
      putAll(
        mapOf(
          "key" to "value",
          "complex" to mapOf(
            "child1" to 1,
            "child2" to "small"
          )
        )
      )
    }


    task1 = emptyTask().apply {
      this.taskId = UUID.randomUUID().toString()
      this.name = "task 1"
      this.businessKey = "ZZZ-1-YYY-2"
      this.description = "some random description"
      this.authorizedPrincipals = mutableSetOf(user("kermit").toString(), group("muppets").toString())
      this.correlations = mutableSetOf(DataEntryId(entryType = "data-entry", entryId = "id"))
      this.payload = payload.toPayloadJson(objectMapper)
      this.payloadAttributes = payload.toJsonPathsWithValues().map { attr -> PayloadAttribute(attr) }.toMutableSet()
      this.dueDate = tomorrow
      this.followUpDate = dayAfterTomorrow
    }
    task2 = emptyTask().apply {
      this.taskId = UUID.randomUUID().toString()
      this.name = "task 2"
      this.description = "Another than TaSk 1"
      this.businessKey = "business-key"
      this.authorizedPrincipals = mutableSetOf(user("ironman").toString(), group("avengers").toString(), group("muppets").toString())
      this.sourceReference.applicationName = "other-app"
      this.sourceReference.name = "other-process"
    }

    entityManager.persist(task1)
    entityManager.persist(task2)

    entityManager.flush()
  }


  @AfterEach
  fun `clean up`() {
    dbCleaner.cleanup()
  }

  @Test
  fun `should find all`() {
    val result = taskRepository.findAll()
    assertThat(result).hasSize(2)
    assertThat(result).containsExactlyInAnyOrder(task1, task2)
  }

  @Test
  fun `should find by authorization`() {
    val all = taskRepository.findAll(isAuthorizedFor(listOf()))
    assertThat(all).hasSize(2)

    val result = taskRepository.findAll(isAuthorizedFor(listOf(user("kermit"))))
    assertThat(result).hasSize(1)
    assertThat(result).containsExactlyInAnyOrder(task1)
  }

  @Test
  fun `should find by application`() {
    val result = taskRepository.findAll(hasApplication("other-app"))
    assertThat(result).hasSize(1)
    assertThat(result).containsExactlyInAnyOrder(task2)
  }

  @Test
  fun `should find by task name like`() {
    val result = taskRepository.findAll(likeName("ask 2"))
    assertThat(result).hasSize(1)
    assertThat(result).containsExactlyInAnyOrder(task2)
  }

  @Test
  fun `should find by task name like or description like`() {
    val result = taskRepository.findAll(
      likeTextSearch("task 1")
    )
    assertThat(result).hasSize(2)
    assertThat(result).containsExactlyInAnyOrder(task1, task2)
  }

  @Test
  fun `should find by task description like`() {
    val result = taskRepository.findAll(likeDescription("random"))
    assertThat(result).hasSize(1)
    assertThat(result).containsExactlyInAnyOrder(task1)
  }

  @Test
  fun `should find by task business like`() {
    val result = taskRepository.findAll(likeBusinessKey("z-1-y"))
    assertThat(result).hasSize(1)
    assertThat(result).containsExactlyInAnyOrder(task1)
  }


  @Test
  fun `should find by process name`() {
    val result = taskRepository.findAll(hasProcessName("other-process"))
    assertThat(result).hasSize(1)
    assertThat(result).containsExactlyInAnyOrder(task2)
  }

  @Test
  fun `should find by process name like`() {
    val result = taskRepository.findAll(likeProcessName("other-process".substring(3, 7)))
    assertThat(result).hasSize(1)
    assertThat(result).containsExactlyInAnyOrder(task2)
  }


  @Test
  fun `should find by business key`() {
    val result = taskRepository.findAll(hasBusinessKey("business-key"))
    assertThat(result).hasSize(1)
    assertThat(result).containsExactlyInAnyOrder(task2)
  }

  @Test
  fun `should find by payload attribute`() {
    val result = taskRepository.findAll(hasTaskPayloadAttribute("complex.child2", "small"))
    assertThat(result).hasSize(1)
    assertThat(result).containsExactlyInAnyOrder(task1)

    val notfound = taskRepository.findAll(hasTaskPayloadAttribute("complex.child1", "13"))
    assertThat(notfound).isEmpty()
  }

  @Test
  fun `should find by due date`() {
    val result = taskRepository.findAll(hasDueDate(tomorrow))
    assertThat(result).hasSize(1)
    assertThat(result).containsExactlyInAnyOrder(task1)
  }

  @Test
  fun `should find by due date before`() {
    val result = taskRepository.findAll(hasDueDateBefore(tomorrow))
    assertThat(result).hasSize(1)
    assertThat(result).containsExactlyInAnyOrder(task2)
  }

  @Test
  fun `should find by due date after`() {
    val result = taskRepository.findAll(hasDueDateAfter(tomorrow))
    assertThat(result).hasSize(1)
    assertThat(result).containsExactlyInAnyOrder(task2)
  }

  @Test
  fun `should find by follow-up date`() {
    val result = taskRepository.findAll(hasFollowUpDate(dayAfterTomorrow))
    assertThat(result).hasSize(1)
    assertThat(result).containsExactlyInAnyOrder(task1)
  }

  @Test
  fun `should find by follow-up date before`() {
    val result = taskRepository.findAll(hasFollowUpDateBefore(twoDaysAfterTomorrow))
    assertThat(result).hasSize(2)
    assertThat(result).containsExactlyInAnyOrder(task1, task2)
  }

  @Test
  fun `should find by follow-up date after`() {
    val result = taskRepository.findAll(hasFollowUpDateAfter(dayAfterTomorrow))
    assertThat(result).hasSize(1)
    assertThat(result).containsExactlyInAnyOrder(task2)
  }

}
