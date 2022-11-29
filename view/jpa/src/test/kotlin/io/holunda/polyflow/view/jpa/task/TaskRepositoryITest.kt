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
import io.holunda.polyflow.view.jpa.task.TaskRepository.Companion.hasTaskPayloadAttribute
import io.holunda.polyflow.view.jpa.task.TaskRepository.Companion.isAuthorizedFor
import org.assertj.core.api.Assertions.assertThat
import org.camunda.bpm.engine.variable.Variables.createVariables
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.util.*
import javax.persistence.EntityManager

@ExtendWith(SpringExtension::class)
@DataJpaTest(showSql = false)
@ContextConfiguration(classes = [TestApplicationDataJpa::class])
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
      this.authorizedPrincipals = mutableSetOf(user("kermit").toString(), group("muppets").toString())
      this.correlations = mutableSetOf(DataEntryId(entryType = "data-entry", entryId = "id"))
      this.payload = payload.toPayloadJson(objectMapper)
      this.payloadAttributes = payload.toJsonPathsWithValues().map { attr -> PayloadAttribute(attr) }.toMutableSet()
    }
    task2 = emptyTask().apply {
      taskId = UUID.randomUUID().toString()
      name = "task 2"
      businessKey = "business-key"
      authorizedPrincipals = mutableSetOf(user("ironman").toString(), group("avengers").toString(), group("muppets").toString())
      sourceReference.applicationName = "other-app"
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

}
