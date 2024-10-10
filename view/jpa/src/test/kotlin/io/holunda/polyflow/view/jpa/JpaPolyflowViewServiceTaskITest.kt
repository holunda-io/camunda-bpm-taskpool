package io.holunda.polyflow.view.jpa

import com.fasterxml.jackson.databind.ObjectMapper
import io.holixon.axon.gateway.query.RevisionValue
import io.holunda.camunda.taskpool.api.business.*
import io.holunda.camunda.taskpool.api.task.TaskAssignedEngineEvent
import io.holunda.camunda.taskpool.api.task.TaskAttributeUpdatedEngineEvent
import io.holunda.camunda.taskpool.api.task.TaskCompletedEngineEvent
import io.holunda.camunda.taskpool.api.task.TaskCreatedEngineEvent
import io.holunda.camunda.variable.serializer.serialize
import io.holunda.polyflow.view.Task
import io.holunda.polyflow.view.TaskWithDataEntries
import io.holunda.polyflow.view.auth.User
import io.holunda.polyflow.view.jpa.itest.TestApplication
import io.holunda.polyflow.view.jpa.process.toSourceReference
import io.holunda.polyflow.view.query.data.DataEntriesForUserQuery
import io.holunda.polyflow.view.query.task.*
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.data.MapEntry
import org.axonframework.messaging.MetaData
import org.axonframework.queryhandling.GenericSubscriptionQueryUpdateMessage
import org.axonframework.queryhandling.QueryUpdateEmitter
import org.axonframework.queryhandling.SubscriptionQueryUpdateMessage
import org.camunda.bpm.engine.variable.Variables.createVariables
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.atLeast
import org.mockito.kotlin.clearInvocations
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit
import java.util.*
import java.util.function.Predicate


@SpringBootTest(
  classes = [TestApplication::class],
  properties = [
    "polyflow.view.jpa.stored-items=task,data-entry"
  ]
)
@ActiveProfiles("itest-tc-mariadb", "mock-query-emitter")
@Transactional
internal class JpaPolyflowViewServiceTaskITest {

  private val emittedQueryUpdates: MutableList<QueryUpdate<Any>> = mutableListOf()

  @Autowired
  lateinit var queryUpdateEmitter: QueryUpdateEmitter

  @Autowired
  lateinit var jpaPolyflowViewService: JpaPolyflowViewTaskService

  @Autowired
  lateinit var jpaPolyflowViewDataEntryService: JpaPolyflowViewDataEntryService

  @Autowired
  lateinit var dbCleaner: DbCleaner

  @Autowired
  lateinit var objectMapper: ObjectMapper

  private val id = UUID.randomUUID().toString()
  private val id2 = UUID.randomUUID().toString()
  private val id3 = UUID.randomUUID().toString()
  private val id4 = UUID.randomUUID().toString()
  private val dataId1 = UUID.randomUUID().toString()
  private val dataType1 = "io.polyflow.test1"
  private val dataId2 = UUID.randomUUID().toString()
  private val dataType2 = "io.polyflow.test2"
  private val now = Instant.now()

  @BeforeEach
  fun `ingest events`() {
    jpaPolyflowViewService.on(
      event = TaskCreatedEngineEvent(
        id = id,
        taskDefinitionKey = "task.def.0815",
        name = "task name 1",
        priority = 50,
        sourceReference = processReference().toSourceReference(),
        payload = createVariables().apply { putAll(createPayload()) },
        businessKey = "business-1",
        createTime = Date.from(Instant.now()),
        candidateUsers = setOf("kermit"),
        candidateGroups = setOf("muppets"),
        dueDate = Date.from(now)
      ), metaData = MetaData.emptyInstance()
    )

    jpaPolyflowViewService.on(
      event = TaskAssignedEngineEvent(
        id = id,
        taskDefinitionKey = "task.def.0815",
        name = "task name 1",
        priority = 25,
        sourceReference = processReference().toSourceReference(),
        payload = createVariables().apply { putAll(createPayload()) },
        businessKey = "business-1",
        createTime = Date.from(Instant.now()),
        candidateUsers = setOf("kermit"),
        candidateGroups = setOf("muppets"),
        assignee = "kermit"
      ), metaData = MetaData.emptyInstance()
    )

    jpaPolyflowViewService.on(
      event = TaskCreatedEngineEvent(
        id = id2,
        taskDefinitionKey = "task.def.0815",
        name = "task name 2",
        priority = 10,
        sourceReference = processReference().toSourceReference(),
        payload = createVariables().apply { putAll(createPayload()) },
        businessKey = "business-2",
        createTime = Date.from(Instant.now()),
        candidateUsers = setOf("piggy"),
        candidateGroups = setOf("muppets")
      ), metaData = MetaData.emptyInstance()
    )

    jpaPolyflowViewService.on(
      event = TaskCompletedEngineEvent(
        id = id2,
        taskDefinitionKey = "task.def.0815",
        name = "task name 2",
        priority = 10,
        sourceReference = processReference().toSourceReference(),
        payload = createVariables().apply { putAll(createPayload()) },
        businessKey = "business-2",
        createTime = Date.from(Instant.now()),
        assignee = "piggy",
        candidateUsers = setOf("piggy"),
        candidateGroups = setOf("muppets")
      ), metaData = MetaData.emptyInstance()
    )

    // for testing: fun query(query: TaskWithDataEntriesForIdQuery)
    jpaPolyflowViewService.on(
      event = TaskCreatedEngineEvent(
        id = id3,
        taskDefinitionKey = "task.def.0815",
        name = "task name 3",
        priority = 10,
        sourceReference = processReference().toSourceReference(),
        payload = createVariables().apply { putAll(createPayload()) },
        correlations = newCorrelations().apply { put(dataType1, dataId1) },
        businessKey = "business-3",
        createTime = Date.from(Instant.now()),
        candidateUsers = setOf("luffy"),
        candidateGroups = setOf("strawhats"),
        dueDate = Date.from(now.plus(1, ChronoUnit.DAYS))
      ), metaData = MetaData.emptyInstance()
    )

    jpaPolyflowViewDataEntryService.on(
      event = DataEntryCreatedEvent(
        entryType = dataType1,
        entryId = dataId1,
        type = "Test",
        applicationName = "test-application",
        name = "Test Entry 1",
        state = ProcessingType.IN_PROGRESS.of("In progress"),
        payload = serialize(payload = createPayload("dataEntry1"), mapper = objectMapper),
        authorizations = listOf(
          AuthorizationChange.addUser("luffy"),
          AuthorizationChange.addGroup("strawhats")
        ),
        createModification = Modification(
          time = OffsetDateTime.ofInstant(Instant.now(), ZoneOffset.UTC),
          username = "luffy",
          log = "strawhats",
          logNotes = "Created the entry"
        )
      ),
      metaData = RevisionValue(revision = 1).toMetaData(),
      now
    )

    // for testing: fun query(query: TasksWithDataEntriesForUserQuery)
    jpaPolyflowViewService.on(
      event = TaskCreatedEngineEvent(
        id = id4,
        taskDefinitionKey = "task.def.0815",
        name = "task name 4",
        priority = 10,
        sourceReference = processReference().toSourceReference(),
        payload = createVariables().apply { putAll(createPayload("otherValue")) },
        correlations = newCorrelations().apply {
          put(dataType1, dataId1)
          put(dataType2, dataId2)
        },
        assignee = "zoro",
        businessKey = "business-4",
        createTime = Date.from(Instant.now()),
        candidateUsers = setOf("zoro"),
        candidateGroups = setOf("strawhats"),
        dueDate = Date.from(now.plus(5, ChronoUnit.DAYS))
      ), metaData = MetaData.emptyInstance()
    )

    jpaPolyflowViewDataEntryService.on(
      event = DataEntryCreatedEvent(
        entryType = dataType2,
        entryId = dataId2,
        type = "Test",
        applicationName = "test-application",
        name = "Test Entry 2",
        state = ProcessingType.IN_PROGRESS.of("In progress"),
        payload = serialize(payload = createPayload("dataEntry2"), mapper = objectMapper),
        authorizations = listOf(
          AuthorizationChange.addUser("zoro")
        ),
        createModification = Modification(
          time = OffsetDateTime.ofInstant(Instant.now(), ZoneOffset.UTC),
          username = "zoro",
          log = "Created",
          logNotes = "Created the entry"
        )
      ),
      metaData = RevisionValue(revision = 1).toMetaData(),
      now
    )

    jpaPolyflowViewService.on(
      event = TaskAttributeUpdatedEngineEvent(
        id = id4,
        taskDefinitionKey = "task.def.0815",
        name = "task name 4",
        priority = 10,
        sourceReference = processReference().toSourceReference(),
        payload = createVariables().apply { putAll(createPayload("otherValue")) },
        correlations = newCorrelations().apply {
          put(dataType1, dataId1)
          put(dataType2, dataId2)
        },
        businessKey = "business-4",
      ), metaData = MetaData.emptyInstance()
    )
  }

  @AfterEach
  fun `cleanup projection`() {
    dbCleaner.cleanup()
    // clear updates
    emittedQueryUpdates.clear()
    clearInvocations(queryUpdateEmitter)
  }

  @Test
  fun `should find the task by id`() {
    val byId1 = jpaPolyflowViewService.query(TaskForIdQuery(id = id))
    assertThat(byId1).isPresent
    assertThat(byId1.get().id).isEqualTo(id)
  }

  @Test
  fun `should find the task with data entries by id`() {
    val byId3 = jpaPolyflowViewService.query(TaskWithDataEntriesForIdQuery(id = id3))
    assertThat(byId3).isPresent
    assertThat(byId3.get().task.id).isEqualTo(id3)
    assertThat(byId3.get().dataEntries).isNotEmpty.hasSize(1)
    assertThat(byId3.get().dataEntries.first().entryId).isEqualTo(dataId1)
  }

  @Test
  fun `should find the task by user with data entries`() {
    val zoro = jpaPolyflowViewService.query(TasksWithDataEntriesForUserQuery(user = User("zoro", setOf()), assignedToMeOnly = false))
    assertThat(zoro.elements).isNotEmpty.hasSize(1)
    assertThat(zoro.elements[0].task.id).isEqualTo(id4)
    assertThat(zoro.elements[0].task.name).isEqualTo("task name 4")
    assertThat(zoro.elements[0].dataEntries).isNotEmpty.hasSize(1)
    assertThat(zoro.elements[0].dataEntries[0].entryId).isEqualTo(dataId2)
    assertThat(zoro.elements[0].task.correlations).containsOnly(
      MapEntry.entry("io.polyflow.test1", dataId1),
      MapEntry.entry("io.polyflow.test2", dataId2)
    )

    val strawhats = jpaPolyflowViewService.query(TasksWithDataEntriesForUserQuery(user = User("other", setOf("strawhats")), assignedToMeOnly = false))
    assertThat(strawhats.elements).isNotEmpty.hasSize(2)
    assertThat(strawhats.elements.map { it.task.id }).contains(id3, id4)
    assertThat(strawhats.elements[0].dataEntries).hasSize(1)
    assertThat(strawhats.elements[0].dataEntries[0].entryId).isEqualTo(dataId1)
    assertThat(strawhats.elements[1].dataEntries).hasSize(1)
    assertThat(strawhats.elements[1].dataEntries[0].entryId).isEqualTo(dataId1)
  }

  @Test
  fun `should find the task by user with data entries and sort results correctly`() {
    val strawhats = jpaPolyflowViewService.query(TasksWithDataEntriesForUserQuery(
      user = User("other", setOf("strawhats")),
      sort = listOf("+name"),
      assignedToMeOnly = false
    ))
    val strawhatsInverse = jpaPolyflowViewService.query(TasksWithDataEntriesForUserQuery(
      user = User("other", setOf("strawhats")),
      sort = listOf("-name"),
      assignedToMeOnly = false
    ))

    assertThat(strawhats.elements).isNotEmpty.hasSize(2)
    assertThat(strawhats.elements.map { it.task.id }).containsExactly(id3, id4)
    assertThat(strawhatsInverse.elements).isNotEmpty.hasSize(2)
    assertThat(strawhatsInverse.elements.map { it.task.id }).containsExactly(id4, id3)
  }

  @Suppress("DEPRECATION")
  @Test
  fun `should sort with empty string, null or empty list correctly`() {
    val sortWithNullQuery = jpaPolyflowViewService.query(AllTasksWithDataEntriesQuery(
      sort = null
    ))

    val sortWithEmptyStringQuery =jpaPolyflowViewService.query(AllTasksWithDataEntriesQuery(
      sort = ""
    ))

    val sortWithEmptyListQuery =jpaPolyflowViewService.query(AllTasksWithDataEntriesQuery(
      sort = listOf()
    ))

    val sortWithSortNotSuppliedQuery = jpaPolyflowViewService.query(AllTasksWithDataEntriesQuery())


    assertThat(sortWithNullQuery.elements).isEqualTo(sortWithEmptyStringQuery.elements)
    assertThat(sortWithEmptyStringQuery.elements).isEqualTo(sortWithEmptyListQuery.elements)
    assertThat(sortWithEmptyListQuery.elements).isEqualTo(sortWithSortNotSuppliedQuery.elements)
    assertThat(sortWithSortNotSuppliedQuery.elements).isEqualTo(sortWithNullQuery.elements)
  }

  @Test
  fun `should find the task with data entries and sort by multiple correctly`() {
    val query = jpaPolyflowViewService.query(AllTasksWithDataEntriesQuery(
      sort = listOf("+priority", "-name")
    ))

    val inverseQuery = jpaPolyflowViewService.query(AllTasksWithDataEntriesQuery(
      sort = listOf("-priority", "+name")
    ))
    assertThat(query.elements.map { it.task.id }).containsExactly(id4, id3, id)
    assertThat(inverseQuery.elements.map { it.task.id }).containsExactly(id, id3, id4)
  }

  @Test
  fun `should not execute query because of wrong sort`() {
    val user = User("other", setOf("strawhats"))
    assertThat(assertThrows<IllegalArgumentException> {
      jpaPolyflowViewService.query(TasksWithDataEntriesForUserQuery(
        user = user,
        sort = listOf("+createdTime"), // entity property
        assignedToMeOnly = false
      ))
    }.message).startsWith("Sort parameter must be one of ").endsWith(" but it was createdTime.")

    assertThat(assertThrows<IllegalArgumentException> {
      jpaPolyflowViewService.query(TasksWithDataEntriesForUserQuery(
        user = user,
        sort = listOf("+candidateUsers"), // unsupported by JPA view
        assignedToMeOnly = false
      ))
    }.message).isEqualTo("'candidateUsers' is not supported for sorting in JPA View")

    assertThat(assertThrows<IllegalArgumentException> {
      jpaPolyflowViewService.query(TasksWithDataEntriesForUserQuery(
        user = user,
        sort = listOf("*name"), // wrong order
        assignedToMeOnly = false
      ))
    }.message).isEqualTo("Sort must start either with '+' or '-' but it was starting with '*'")

    assertThat(assertThrows<IllegalArgumentException> {
      jpaPolyflowViewService.query(AllTasksWithDataEntriesQuery(
        sort = listOf("")
      ))
    }.message).isEqualTo("Sort parameter must not be blank")

  }

  @Test
  fun `should find the task by group with data entries`() {
    val strawhats = jpaPolyflowViewService.query(TasksWithDataEntriesForGroupQuery(user = User("some", setOf("strawhats")), includeAssigned = false))
    assertThat(strawhats.elements).isNotEmpty.hasSize(1)
    assertThat(strawhats.elements.map { it.task.id }).contains(id3)
    assertThat(strawhats.elements[0].task.assignee).isNull()
    assertThat(strawhats.elements[0].dataEntries).hasSize(1)
    assertThat(strawhats.elements[0].dataEntries[0].entryId).isEqualTo(dataId1)

    val strawhats2 = jpaPolyflowViewService.query(TasksWithDataEntriesForGroupQuery(user = User("some", setOf("strawhats")), includeAssigned = true))
    assertThat(strawhats2.elements).isNotEmpty.hasSize(1)
    assertThat(strawhats2.elements.map { it.task.id }).contains(id4)
    assertThat(strawhats2.elements[0].task.assignee).isNotNull()
    assertThat(strawhats2.elements[0].dataEntries).hasSize(1)
    assertThat(strawhats2.elements[0].dataEntries[0].entryId).isEqualTo(dataId1)

  }

  @Test
  fun `should find the task by user`() {
    val kermit = jpaPolyflowViewService.query(TasksForUserQuery(user = User("kermit", setOf()), assignedToMeOnly = false))
    assertThat(kermit.elements).isNotEmpty
    assertThat(kermit.elements[0].id).isEqualTo(id)
    assertThat(kermit.elements[0].name).isEqualTo("task name 1")
    val muppets = jpaPolyflowViewService.query(TasksForUserQuery(user = User("other", setOf("muppets")), assignedToMeOnly = false))
    assertThat(muppets.elements).isNotEmpty
    assertThat(muppets.elements[0].id).isEqualTo(id)
  }

  @Test
  fun `should find the task by user assigned to me`() {
    val luffy = jpaPolyflowViewService.query(TasksForUserQuery(user = User("luffy", setOf()), assignedToMeOnly = false))
    assertThat(luffy.elements).isNotEmpty
    assertThat(luffy.elements[0].id).isEqualTo(id3)
    assertThat(luffy.elements[0].name).isEqualTo("task name 3")

    val zoro = jpaPolyflowViewService.query(TasksForUserQuery(user = User("zoro", setOf()), assignedToMeOnly = true))
    assertThat(zoro.elements).isNotEmpty
    assertThat(zoro.elements[0].id).isEqualTo(id4)
    assertThat(zoro.elements[0].name).isEqualTo("task name 4")

  }

  @Test
  fun `should or-compose task payload filters on same attribute`() {
    val query = jpaPolyflowViewService.query(
      TasksForUserQuery(
        user = User("zoro", setOf("strawhats")),
        assignedToMeOnly = false,
        filters = listOf("key=value", "key=otherValue", "key=anotherValue")
      )
    )
    assertThat(query.elements).hasSize(2)
  }

  @Test
  fun `should or-compose data entry payload filters on same attribute`() {
    val query = jpaPolyflowViewDataEntryService.query(
      DataEntriesForUserQuery(
        user = User("zoro", setOf("strawhats")),
        filters = listOf("key=dataEntry1", "key=dataEntry2", "key=dataEntryFoo")
      )
    )
    assertThat(query.payload.elements).hasSize(2)
  }

  @Test
  fun `should or-compose task attribute filters on same attribute`() {
    val kermit = jpaPolyflowViewService.query(
      TasksForUserQuery(
        user = User("zoro", setOf("strawhats")),
        assignedToMeOnly = false,
        filters = listOf("task.businessKey=business-3", "task.businessKey=business-4")
      )
    )
    assertThat(kermit.elements).hasSize(2)
  }

  @Test
  fun `should or-compose date entry attribute filters on same attribute`() {
    val kermit = jpaPolyflowViewService.query(
      TasksWithDataEntriesForUserQuery(
        user = User("zoro", setOf("strawhats")),
        assignedToMeOnly = false,
        filters = listOf("data.entryId=${dataId1}", "data.entryId=${dataId2}")
      )
    )
    assertThat(kermit.elements).hasSize(2)
  }

  @Test
  fun `should find the task by group`() {
    val unassigned = jpaPolyflowViewService.query(TasksForGroupQuery(user = User("other", setOf("muppets")), includeAssigned = false))
    assertThat(unassigned.elements).isEmpty()

    val assigned = jpaPolyflowViewService.query(TasksForGroupQuery(user = User("other", setOf("muppets")), includeAssigned = true))
    assertThat(assigned.elements).hasSize(1)
    assertThat(assigned.elements[0].id).isEqualTo(id)
    assertThat(assigned.elements[0].name).isEqualTo("task name 1")
  }

  @Test
  fun `should find the task by candidate user and group`() {
    val unassigned = jpaPolyflowViewService.query(TasksForCandidateUserAndGroupQuery(user = User("zoro", setOf("muppets")), includeAssigned = false))
    assertThat(unassigned.elements).isEmpty()

    val assigned = jpaPolyflowViewService.query(TasksForCandidateUserAndGroupQuery(user = User("zoro", setOf("muppets")), includeAssigned = true))
    assertThat(assigned.elements).hasSize(2)
    assertThat(assigned.elements[0].id).isEqualTo(id4)
    assertThat(assigned.elements[0].name).isEqualTo("task name 4")
    assertThat(assigned.elements[1].id).isEqualTo(id)
    assertThat(assigned.elements[1].name).isEqualTo("task name 1")

    val assignedToZoro = jpaPolyflowViewService.query(TasksForCandidateUserAndGroupQuery(user = User("zoro", setOf("muppets")), includeAssigned = true, filters = listOf("task.assignee=zoro")))
    assertThat(assignedToZoro.elements).hasSize(1)
    assertThat(assignedToZoro.elements[0].id).isEqualTo(id4)
    assertThat(assignedToZoro.elements[0].name).isEqualTo("task name 4")
  }


  @Test
  fun `query updates are sent`() {
    captureEmittedQueryUpdates()
    assertThat(emittedQueryUpdates).hasSize(41)

    assertThat(emittedQueryUpdates.filter { it.queryType == TaskForIdQuery::class.java && it.asTask().id == id }).hasSize(2)
    assertThat(emittedQueryUpdates.filter { it.queryType == TaskForIdQuery::class.java && it.asTask().id == id2 }).hasSize(2)
    assertThat(emittedQueryUpdates.filter { it.queryType == TaskForIdQuery::class.java && it.asTask().id == id2 && it.asTask().deleted }).hasSize(1)

    assertThat(emittedQueryUpdates.filter { it.queryType == TaskWithDataEntriesForIdQuery::class.java && it.asTaskWithDataEntries().task.id == id }).hasSize(2)
    assertThat(emittedQueryUpdates.filter { it.queryType == TaskWithDataEntriesForIdQuery::class.java && it.asTaskWithDataEntries().task.id == id2 }).hasSize(2)
    assertThat(emittedQueryUpdates.filter { it.queryType == TaskWithDataEntriesForIdQuery::class.java && it.asTaskWithDataEntries().task.id == id2 && it.asTaskWithDataEntries().task.deleted })
      .hasSize(1)

    assertThat(emittedQueryUpdates.filter {
      it.queryType == TasksForApplicationQuery::class.java && it.asTaskQueryResult().elements.map { task -> task.id }.contains(id)
    }).hasSize(2)
    assertThat(emittedQueryUpdates.filter {
      it.queryType == TasksForApplicationQuery::class.java && it.asTaskQueryResult().elements.map { task -> task.id }.contains(id2)
    }).hasSize(2)

    assertThat(emittedQueryUpdates.filter {
      it.queryType == TasksForUserQuery::class.java && it.asTaskQueryResult().elements.map { task -> task.id }.contains(id)
    }).hasSize(2)
    assertThat(emittedQueryUpdates.filter {
      it.queryType == TasksForUserQuery::class.java && it.asTaskQueryResult().elements.map { task -> task.id }.contains(id2)
    }).hasSize(2)

    assertThat(emittedQueryUpdates.filter {
      it.queryType == TasksWithDataEntriesForUserQuery::class.java && it.asTaskWithDataEntriesQueryResult().elements.map { taskW -> taskW.task.id }.contains(id)
    }).hasSize(2)
    assertThat(emittedQueryUpdates.filter {
      it.queryType == TasksWithDataEntriesForUserQuery::class.java && it.asTaskWithDataEntriesQueryResult().elements.map { taskW -> taskW.task.id }
        .contains(id2)
    }).hasSize(2)

  }

  @Test
  fun `should count tasks`() {

    val counts = jpaPolyflowViewService.query(TaskCountByApplicationQuery())
    assertThat(counts).isNotNull
    assertThat(counts).hasSize(1)
    assertThat(counts[0].application).isEqualTo("test-application")
    assertThat(counts[0].taskCount).isEqualTo(3)
  }

  @Test
  fun `should find task attribute names`() {
    // Some for zoro in muppets
    val names = jpaPolyflowViewService.query(TaskAttributeNamesQuery(user = User("zoro", setOf("muppets"))))
    assertThat(names).isNotNull
    assertThat(names.elements).hasSize(4)
    assertThat(names.elements).contains("key", "key-int", "complex.attribute1", "complex.attribute2")

    // But none for bud in heros
    val namesOSH = jpaPolyflowViewService.query(TaskAttributeNamesQuery(user = User("bud", setOf("old_school_heros"))))
    assertThat(namesOSH).isNotNull
    assertThat(namesOSH.elements).hasSize(0)
  }

  @Test
  fun `should find task attribute values`() {
    // Some for zoro in muppets
    val names = jpaPolyflowViewService.query(TaskAttributeValuesQuery(user = User("zoro", setOf("muppets")), attributeName = "key"))
    assertThat(names).isNotNull
    assertThat(names.elements).hasSize(2)
    assertThat(names.elements).contains("value", "otherValue")

    // But none for bud in heros
    val namesOSH = jpaPolyflowViewService.query(TaskAttributeValuesQuery(user = User("bud", setOf("old_school_heros")), attributeName = "key"))
    assertThat(namesOSH).isNotNull
    assertThat(namesOSH.elements).hasSize(0)
  }

  @Test
  fun `should find tasks by date range`() {
    val range = jpaPolyflowViewService.query(
      AllTasksQuery(
        filters = listOf("task.dueDate[]${now.minus(1, ChronoUnit.DAYS)}|${now.plus(2, ChronoUnit.DAYS)}")
      )
    )
    assertThat(range.elements).hasSize(2)
  }

  @Test
  fun `should not find tasks outsides of date range`() {
    val range = jpaPolyflowViewService.query(
      AllTasksQuery(
        filters = listOf("task.dueDate[]${now.minus(5, ChronoUnit.DAYS)}|${now.minus(2, ChronoUnit.DAYS)}")
      )
    )
    assertThat(range.elements).isEmpty()
  }

  private fun captureEmittedQueryUpdates(): List<QueryUpdate<Any>> {
    val queryTypeCaptor = argumentCaptor<Class<Any>>()
    val predicateCaptor = argumentCaptor<Predicate<Any>>()
    val updateCaptor = argumentCaptor<SubscriptionQueryUpdateMessage<Any>>()
    verify(queryUpdateEmitter, atLeast(0)).emit(queryTypeCaptor.capture(), predicateCaptor.capture(), updateCaptor.capture())
    clearInvocations(queryUpdateEmitter)

    val foundUpdates = queryTypeCaptor.allValues
      .zip(predicateCaptor.allValues)
      .zip(updateCaptor.allValues) { (queryType, predicate), update ->
        QueryUpdate(queryType, predicate, update)
      }

    emittedQueryUpdates.addAll(foundUpdates)
    return foundUpdates
  }

  data class QueryUpdate<E>(val queryType: Class<E>, val predicate: Predicate<E>, val update: Any) {
    @Suppress("UNCHECKED_CAST")
    fun asTask(): Task = (this.update as GenericSubscriptionQueryUpdateMessage<Task>).payload

    @Suppress("UNCHECKED_CAST")
    fun asTaskWithDataEntries(): TaskWithDataEntries = (this.update as GenericSubscriptionQueryUpdateMessage<TaskWithDataEntries>).payload

    @Suppress("UNCHECKED_CAST")
    fun asTaskQueryResult(): TaskQueryResult = (this.update as GenericSubscriptionQueryUpdateMessage<TaskQueryResult>).payload

    @Suppress("UNCHECKED_CAST")
    fun asTaskWithDataEntriesQueryResult(): TasksWithDataEntriesQueryResult =
      (this.update as GenericSubscriptionQueryUpdateMessage<TasksWithDataEntriesQueryResult>).payload

  }

  private fun createPayload(value: String = "value"): Map<String, Any> {
    return mapOf(
      "key" to value,
      "key-int" to 1,
      "complex.attribute1" to "value",
      "complex.attribute2" to Date.from(now),
      "complexIgnored" to Pojo( // Normally, the event will never have a complex object like this in the payload. (Got already deserialized by the sender in ProjectingCommandAccumulator.serializePayloadIfNeeded)
        attribute1 = "value",
        attribute2 = Date.from(now)
      )
    )
  }

}
