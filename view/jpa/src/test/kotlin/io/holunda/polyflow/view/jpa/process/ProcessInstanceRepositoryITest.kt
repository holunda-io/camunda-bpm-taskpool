package io.holunda.polyflow.view.jpa.process

import io.holunda.polyflow.view.ProcessInstanceState
import io.holunda.polyflow.view.jpa.process.ProcessInstanceRepository.Companion.hasStates
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
class ProcessInstanceRepositoryITest {
  @Autowired
  lateinit var entityManager: EntityManager
  @Autowired
  lateinit var processInstanceRepository: ProcessInstanceRepository

  lateinit var processInstance: ProcessInstanceEntity

  @Before
  fun `insert entity`() {

    val id = UUID.randomUUID().toString()

    processInstance = ProcessInstanceEntity(
      processInstanceId = id,
      businessKey = "businessKey",
      superInstanceId = null,
      startActivityId = "start",
      startUserId = "kermit",
      sourceReference = SourceReferenceEmbeddable(
        instanceId = id,
        executionId = UUID.randomUUID().toString(),
        definitionId = "my-process-def-key:13",
        definitionKey = "my-process-def-key",
        name = "My process",
        applicationName = "test-application",
        tenantId = null,
        sourceType = "PROCESS"
      ),
      state = ProcessInstanceState.RUNNING,
      deleteReason = null,
      endActivityId = null
    )

    entityManager.persist(processInstance)
    entityManager.flush()
  }

  @After
  fun `clean up`() {
    processInstanceRepository.deleteAll()
    entityManager.flush()
  }

  @Test
  fun `should find process instance by id`() {
    val response = processInstanceRepository.findById(processInstance.processInstanceId)

    assertThat(response).isNotEmpty
    assertThat(response.get()).isEqualTo(processInstance)

    val empty = processInstanceRepository.findById("not-exists")
    assertThat(empty).isEmpty
  }

  @Test
  fun `should find process instance by state`() {
    val response = processInstanceRepository.findAll(hasStates(setOf(ProcessInstanceState.RUNNING)))

    assertThat(response).hasSize(1)
    assertThat(response[0]).isEqualTo(processInstance)

    val empty = processInstanceRepository.findAll(hasStates(setOf(ProcessInstanceState.SUSPENDED)))
    assertThat(empty).isEmpty()
  }

}
