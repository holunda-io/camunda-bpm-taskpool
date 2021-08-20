package io.holunda.polyflow.view.jpa.data

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.holunda.camunda.taskpool.api.business.ProcessingType
import io.holunda.camunda.variable.serializer.toJsonPathsWithValues
import io.holunda.polyflow.view.jpa.auth.AuthorizationPrincipal.Companion.group
import io.holunda.polyflow.view.jpa.auth.AuthorizationPrincipal.Companion.user
import io.holunda.polyflow.view.jpa.data.DataEntryRepository.Companion.hasPayloadAttribute
import io.holunda.polyflow.view.jpa.payload.PayloadAttribute
import org.assertj.core.api.Assertions.assertThat
import org.camunda.bpm.engine.variable.Variables.createVariables
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.data.jpa.domain.Specification.where
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

  lateinit var dataEntry: DataEntryEntity
  lateinit var dataEntry2: DataEntryEntity

  @Before
  fun `insert entries`() {

    val id = UUID.randomUUID().toString()

    val payload1 = createVariables().apply {
      putAll(
        mapOf(
          "amount" to 90L,
          "id" to id,
          "child" to mapOf(
            "key" to "value",
            "key-number" to 42
          )
        )
      )
    }
    val json = jacksonObjectMapper().writeValueAsString(payload1)
    val payloadAttributes = payload1.toJsonPathsWithValues().map { PayloadAttribute(it) }.toSet()

    val payload2 = createVariables().apply {
      putAll(
        mapOf(
          "child" to mapOf(
            "key-number" to 42
          )
        )
      )
    }
    val json2 = jacksonObjectMapper().writeValueAsString(payload2)
    val payloadAttributes2 = payload2.toJsonPathsWithValues().map { PayloadAttribute(it) }.toSet()

    val state1 = ProcessingType.IN_PROGRESS.of("In progress")
    val state2 = ProcessingType.IN_PROGRESS.of("In review")

    dataEntry = DataEntryEntity(
      dataEntryId = DataEntryId(entryType = "test", entryId = id),
      type = "Test Entry",
      name = "Test Case",
      applicationName = "my-application",
      state = DataEntryStateEmbeddable(state1),
      description = "This is a test case.",
      revision = 1L,
      lastModifiedDate = Instant.now(),
      authorizedPrincipals = mutableSetOf(
        group("muppets").toString(),
        user("kermit").toString(),
        user("piggy").toString(),
      ),
      payload = json,
      payloadAttributes = payloadAttributes.toMutableSet(),
    ).apply {
      this.protocol = mutableListOf(
        ProtocolElement(
          dataEntry = this,
          state = DataEntryStateEmbeddable(state1),
          username = "kermit",
          logMessage = "Created",
          logDetails = "Created test case."
        )
      )
    }

    dataEntry2 = DataEntryEntity(
      dataEntryId = DataEntryId(entryType = "test", entryId = UUID.randomUUID().toString()),
      type = "Test Entry",
      name = "Test Case 2",
      applicationName = "my-application",
      state = DataEntryStateEmbeddable(state2),
      revision = 12L,
      description = "This is a second test case.",
      lastModifiedDate = Instant.now(),
      payload = json2,
      payloadAttributes = payloadAttributes2.toMutableSet(),
      authorizedPrincipals = mutableSetOf(
        group("avengers").toString(),
        user("piggy").toString(),
      )
    ).apply {
      this.protocol = mutableListOf(
        ProtocolElement(
          dataEntry = this,
          state = DataEntryStateEmbeddable(state1),
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
  fun `remove all stuff`() {
    dataEntryRepository.deleteAll()
    entityManager.flush()
  }

  @Test
  fun `should find data entry by id`() {
    val found = dataEntryRepository.findByIdOrNull(DataEntryId(entryType = dataEntry.dataEntryId.entryType, entryId = dataEntry.dataEntryId.entryId))
    assertThat(found).isNotNull
    assertThat(found).isEqualTo(dataEntry)
  }

  @Test
  fun `should find data entries by authorization dsl method`() {
    val muppets = dataEntryRepository.findAllByAuthorizedPrincipalsIn(setOf(group("muppets").toString()))
    assertThat(muppets).containsExactly(dataEntry)

    val kermit = dataEntryRepository.findAllByAuthorizedPrincipalsIn(setOf(user("kermit").toString()))
    assertThat(kermit).containsExactly(dataEntry)

    val piggy = dataEntryRepository.findAllByAuthorizedPrincipalsIn(setOf(user("piggy").toString()))
    assertThat(piggy).containsExactlyInAnyOrderElementsOf(listOf(dataEntry, dataEntry2))

    val avengers = dataEntryRepository.findAllByAuthorizedPrincipalsIn(setOf(group("avengers").toString()))
    assertThat(avengers).containsExactly(dataEntry2)

    val unknownGroup = dataEntryRepository.findAllByAuthorizedPrincipalsIn(setOf(group("unknown group").toString()))
    assertThat(unknownGroup).isEmpty()

    val unknownUser = dataEntryRepository.findAllByAuthorizedPrincipalsIn(setOf(user("unknown user").toString()))
    assertThat(unknownUser).isEmpty()
  }

  @Test
  fun `should find all data entries`() {
    val all = dataEntryRepository.findAll()
    assertThat(all).containsExactlyInAnyOrderElementsOf(listOf(dataEntry, dataEntry2))
  }

  @Test
  fun `should find by filter`() {
//    val byStateInProgress = dataEntryRepository.findAll(where(hasState("In progress")))
//    assertThat(byStateInProgress).containsExactlyInAnyOrderElementsOf(listOf(dataEntry))
//
//    val byStateInReview = dataEntryRepository.findAll(where(hasState("In review")))
//    assertThat(byStateInReview).containsExactlyInAnyOrderElementsOf(listOf(dataEntry2))
//
//    val byProcessingTypeInProgress = dataEntryRepository.findAll(where(hasProcessingType(ProcessingType.IN_PROGRESS)))
//    assertThat(byProcessingTypeInProgress).containsExactlyInAnyOrderElementsOf(listOf(dataEntry, dataEntry2))
//
//    val byProcessingTypeCompleted = dataEntryRepository.findAll(where(hasProcessingType(ProcessingType.COMPLETED)))
//    assertThat(byProcessingTypeCompleted).isEmpty()
//
////    val byPayloadFilterByChildKeyNumberValue = dataEntryRepository.findAll(where(hasPayloadAttribute("child.key-number", "42")))
//    val byPayloadFilterByChildKeyNumberValue = dataEntryRepository.findAll(where(hasPayloadAttribute("child.key-number", "42")))
//    assertThat(byPayloadFilterByChildKeyNumberValue).containsExactlyInAnyOrderElementsOf(listOf(dataEntry, dataEntry2))

    val byPayloadFilterByChildKeyValue =
      dataEntryRepository.findAll(
//        where(hasPayloadAttribute("child.key", "value"))
//          .and(hasPayloadAttribute("id", dataEntry.dataEntryId.entryId))
        where(hasPayloadAttribute("child.key", "value"))
          .and(hasPayloadAttribute("id", dataEntry.dataEntryId.entryId))
      )
    assertThat(byPayloadFilterByChildKeyValue).containsExactlyInAnyOrderElementsOf(listOf(dataEntry))

//    val piggy = dataEntryRepository.findAll(isAuthorizedFor(setOf(user("piggy"))))
//    assertThat(piggy).containsExactlyInAnyOrderElementsOf(listOf(dataEntry, dataEntry2))
//
//    val kermitOrAvengers = dataEntryRepository.findAll(isAuthorizedFor(setOf(user("kermit"), group("avengers"))))
//    assertThat(kermitOrAvengers).containsExactlyInAnyOrderElementsOf(listOf(dataEntry, dataEntry2))

  }

}
