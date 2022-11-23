package io.holunda.polyflow.datapool.core.business

import io.holunda.camunda.taskpool.api.business.*
import io.holunda.polyflow.datapool.core.itest.TestApplication
import mu.KLogging
import org.assertj.core.api.Assertions.assertThat
import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.eventhandling.EventBus
import org.axonframework.eventhandling.EventMessage
import org.camunda.bpm.engine.variable.Variables
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension


@SpringBootTest(classes = [TestApplication::class])
@ActiveProfiles("itest-first-event-only")
@ExtendWith(SpringExtension::class)
internal class DataEntryAggregateFirstEventOnlyEventSourcingRepositoryITest {

  companion object: KLogging()

  @Autowired
  private lateinit var commandGateway: CommandGateway

  @Autowired
  private lateinit var eventBus: EventBus

  private val receivedEvents: MutableList<EventMessage<*>> = mutableListOf()

  @BeforeEach
  fun registerHandler() {
    eventBus.subscribe { messages -> receivedEvents.addAll(messages) }
  }

  @Test
  fun `should create aggregate`() {

    val command = CreateOrUpdateDataEntryCommand(
      dataIdentity = dataIdentityString("dataType", "4711"),
      dataEntryChange = DataEntryChange(
        entryId = "4711",
        entryType = "dataType",
        type = "Type",
        applicationName = "itest-application",
        name = "Data entry name",
        correlations = newCorrelations(),
        payload = Variables.createVariables().putValueTyped("key", Variables.stringValue("value")),
        description = "Description of the data entry",
        state = ProcessingType.IN_PROGRESS.of("initial"),
        modification = Modification(log = "Created"),
        authorizationChanges = listOf(AuthorizationChange.addUser("kermit")),
        formKey = "app:form"
      )
    )
    logger.info { "-----------" }
    commandGateway.sendAndWait<String>(command)
    logger.info { "-----------" }
    commandGateway.sendAndWait<String>(
      command.copy(
        dataEntryChange = command.dataEntryChange.copy(
          modification = Modification(log = "Modified"),
          state = ProcessingType.IN_PROGRESS.of("in progress"),
          authorizationChanges = listOf()
        )
      )
    )
    logger.info { "-----------" }
    commandGateway.sendAndWait<String>(
      command.copy(
        dataEntryChange = command.dataEntryChange.copy(
          modification = Modification(log = "Modified again"),
          state = ProcessingType.IN_PROGRESS.of("in progress"),
          authorizationChanges = listOf()
        )
      )
    )
    logger.info { "-----------" }
    commandGateway.sendAndWait<String>(
      command.copy(
        dataEntryChange = command.dataEntryChange.copy(
          modification = Modification(log = "Modified again again"),
          state = ProcessingType.IN_PROGRESS.of("in progress"),
          authorizationChanges = listOf()
        )
      )
    )
    logger.info { "-----------" }

    assertThat(receivedEvents).hasSize(4)
  }
}