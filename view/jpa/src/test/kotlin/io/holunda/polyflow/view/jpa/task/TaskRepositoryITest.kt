package io.holunda.polyflow.view.jpa.task

import io.holunda.polyflow.view.jpa.auth.AuthorizationPrincipal.Companion.group
import io.holunda.polyflow.view.jpa.auth.AuthorizationPrincipal.Companion.user
import io.holunda.polyflow.view.jpa.data.DataEntryId
import io.holunda.polyflow.view.jpa.emptyTask
import io.holunda.polyflow.view.jpa.itest.MockQueryEmitterConfiguration
import io.holunda.polyflow.view.jpa.itest.TestApplicationDataJpa
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringRunner
import java.util.*
import javax.persistence.EntityManager

@RunWith(SpringRunner::class)
@DataJpaTest
@ContextConfiguration(classes = [TestApplicationDataJpa::class])
@ActiveProfiles("itest", "mock-query-emitter")
class TaskRepositoryITest {
  @Autowired
  lateinit var entityManager: EntityManager

  @Autowired
  lateinit var taskRepository: TaskRepository

  lateinit var task1: TaskEntity
  lateinit var task2: TaskEntity

  @Before
  fun `insert entries`() {
    task1 = emptyTask().apply {
      taskId = UUID.randomUUID().toString()
      name = "task 1"
      authorizedPrincipals = mutableSetOf(user("kermit").toString(), group("muppets").toString())
      correlations = mutableSetOf(DataEntryId(entryType = "data-entry", entryId = "id"))
    }
    task2 = emptyTask().apply {
      taskId = UUID.randomUUID().toString()
      name = "task 2"
      authorizedPrincipals = mutableSetOf(user("ironman").toString(), group("avengers").toString(), group("muppets").toString())
    }

    entityManager.persist(task1)
    entityManager.persist(task2)

    entityManager.flush()
  }

  @After
  fun `clean up`() {
    taskRepository.deleteAll()
    entityManager.flush()
  }

  @Test
  fun `should find all`() {
    val result = taskRepository.findAll()
    assertThat(result).hasSize(2)
    assertThat(result).containsExactlyInAnyOrder(task1, task2)
  }

}
