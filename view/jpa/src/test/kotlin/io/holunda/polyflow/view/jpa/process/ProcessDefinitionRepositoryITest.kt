package io.holunda.polyflow.view.jpa.process

import io.holunda.polyflow.view.jpa.auth.AuthorizationPrincipal.Companion.group
import io.holunda.polyflow.view.jpa.process.ProcessDefinitionRepository.Companion.isStarterAuthorizedFor
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner
import java.util.*
import javax.persistence.EntityManager

@RunWith(SpringRunner::class)
@DataJpaTest
@ActiveProfiles("itest")
class ProcessDefinitionRepositoryITest {
  @Autowired
  lateinit var entityManager: EntityManager

  @Autowired
  lateinit var processDefinitionRepository: ProcessDefinitionRepository

  lateinit var processDefinition: ProcessDefinitionEntity

  @Before
  fun `insert entity`() {

    val id = UUID.randomUUID().toString()

    processDefinition = ProcessDefinitionEntity(
      processDefinitionId = id,
      processDefinitionKey = "my-process-def-key",
      processDefinitionVersion = 13,
      applicationName = "application",
      name = "My Process",
      versionTag = "tag1",
      description = "Very long Very long Very long Very long Very long Very long Very long Very long Very long Very long Very long Very long Very long " +
        "Very longVery long Very long Very long Very long Very long Very long Very long Very long Very long Very long Very long Very long Very long Very " +
        "Very longVery long Very long Very long Very long Very long Very long Very long Very long Very long Very long Very long Very long Very long Very " +
        "long Very long Very long Very long Very long Very long ",
      startFormKey = "start-form",
      startableFromTasklist = true,
      authorizedStarterPrincipals = mutableSetOf("GROUP:client-id:group-id")
    )

    entityManager.persist(processDefinition)
    entityManager.flush()
  }

  @After
  fun `clean up`() {
    processDefinitionRepository.deleteAll()
    entityManager.flush()
  }

  @Test
  fun `should find process definition by id`() {
    val response = processDefinitionRepository.findById(processDefinition.processDefinitionId)

    assertThat(response).isNotEmpty
    assertThat(response.get()).isEqualTo(processDefinition)

    val empty = processDefinitionRepository.findById("not-exists")
    assertThat(empty).isEmpty
  }

  @Test
  fun `should find process definition by starter principal`() {
    val response = processDefinitionRepository.findAll(isStarterAuthorizedFor(setOf(group("client-id:group-id"))))

    assertThat(response).hasSize(1)
    assertThat(response[0]).isEqualTo(processDefinition)

    val empty = processDefinitionRepository.findAll(isStarterAuthorizedFor(setOf(group("client-id:other-group"))))
    assertThat(empty).isEmpty()
  }

}
