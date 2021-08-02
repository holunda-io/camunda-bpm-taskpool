package io.holunda.polyflow.view.jpa.data

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.holunda.camunda.taskpool.api.business.ProcessingType
import io.holunda.polyflow.view.jpa.auth.AuthorizationPrincipal
import io.holunda.polyflow.view.jpa.auth.AuthorizationPrincipalRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner
import java.time.Instant
import java.util.*
import javax.persistence.EntityManager

@RunWith(SpringRunner::class)
@DataJpaTest
@ActiveProfiles("itest")
internal class DataEntryRepositoryITest {
  @Autowired
  lateinit var entityManager: EntityManager

  @Autowired
  lateinit var dataEntryRepository: DataEntryRepository

  @Autowired
  lateinit var authorizationPrincipalRepository: AuthorizationPrincipalRepository

  lateinit var dataEntry: DataEntryEntity
  lateinit var dataEntry2: DataEntryEntity

  @Before
  fun `insert principals`() {
    authorizationPrincipalRepository.saveAll(
      setOf(
        AuthorizationPrincipal.group("muppets"),
        AuthorizationPrincipal.user("kermit"),
        AuthorizationPrincipal.user("piggy"),
        AuthorizationPrincipal.group("avengers"),
      )
    )

    val id = UUID.randomUUID().toString()
    val payload = mapOf(
      "amount" to 90L,
      "id" to id
    )

    val json = jacksonObjectMapper().writeValueAsString(payload)

    val id2 = UUID.randomUUID().toString()
    val state = ProcessingType.IN_PROGRESS.of("In progress")

    dataEntry = DataEntryEntity(
      dataEntryId = DataEntryId(entryType = "test", entryId = id),
      type = "Test Entry",
      name = "Test Case",
      applicationName = "my-application",
      state = DataEntryStateEmbeddable(state),
      description = "This is a test case.",
      revision = 1L,
      lastModifiedDate = Instant.now(),
      authorizedPrincipals = setOf(
        AuthorizationPrincipal.group("muppets"),
        AuthorizationPrincipal.user("kermit"),
        AuthorizationPrincipal.user("piggy"),
      ),
      payload = json
    ).apply {
      this.protocol = listOf(
        ProtocolElement(
          dataEntry = this,
          state = DataEntryStateEmbeddable(state),
          username = "kermit",
          logMessage = "Created",
          logDetails = "Created test case."
        )
      )
    }

    dataEntry2 = DataEntryEntity(
      dataEntryId = DataEntryId(entryType = "test", entryId = id2),
      type = "Test Entry",
      name = "Test Case 2",
      applicationName = "my-application",
      state = DataEntryStateEmbeddable(state),
      revision = 12L,
      description = "This is a second test case.",
      lastModifiedDate = Instant.now(),
      authorizedPrincipals = setOf(
        AuthorizationPrincipal.group("avengers"),
        AuthorizationPrincipal.user("piggy"),
      )
    ).apply {
      this.protocol = listOf(
        ProtocolElement(
          dataEntry = this,
          state = DataEntryStateEmbeddable(state),
          username = "ironman",
          logMessage = "Created other",
          logDetails = "Created test case 2."
        )
      )
    }

    entityManager.persist(dataEntry)
    entityManager.persist(dataEntry2)

    entityManager.flush()
  }

  @After
  fun `remove principals`() {
    dataEntryRepository.deleteAll()
    authorizationPrincipalRepository.deleteAll()
    entityManager.flush()
  }

  @Test
  fun `should find data entry by id`() {
    val found = dataEntryRepository.findByIdOrNull(DataEntryId(entryType = dataEntry.dataEntryId.entryType, entryId = dataEntry.dataEntryId.entryId))
    assertThat(found).isNotNull
    assertThat(found).isEqualTo(dataEntry)
  }

  @Test
  fun `should find data entries by authorization`() {
    val muppets = dataEntryRepository.findAllByAuthorizedPrincipalsIn(setOf(AuthorizationPrincipal.group("muppets")))
    assertThat(muppets).containsExactly(dataEntry)

    val kermit = dataEntryRepository.findAllByAuthorizedPrincipalsIn(setOf(AuthorizationPrincipal.user("kermit")))
    assertThat(kermit).containsExactly(dataEntry)

    val piggy = dataEntryRepository.findAllByAuthorizedPrincipalsIn(setOf(AuthorizationPrincipal.user("piggy")))
    assertThat(piggy).containsExactlyInAnyOrderElementsOf(listOf(dataEntry, dataEntry2))

    val avengers = dataEntryRepository.findAllByAuthorizedPrincipalsIn(setOf(AuthorizationPrincipal.group("avengers")))
    assertThat(avengers).containsExactly(dataEntry2)

    val unknownGroup = dataEntryRepository.findAllByAuthorizedPrincipalsIn(setOf(AuthorizationPrincipal.group("unknown group")))
    assertThat(unknownGroup).isEmpty()

    val unknownUser = dataEntryRepository.findAllByAuthorizedPrincipalsIn(setOf(AuthorizationPrincipal.user("unknown user")))
    assertThat(unknownUser).isEmpty()
  }

  @Test
  fun `should find all data entries`() {
    val all = dataEntryRepository.findAll()
    assertThat(all).containsExactlyInAnyOrderElementsOf(listOf(dataEntry, dataEntry2))
  }


}
