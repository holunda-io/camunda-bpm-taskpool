package io.holunda.polyflow.view.jpa

import com.fasterxml.jackson.databind.ObjectMapper
import io.holunda.camunda.taskpool.api.task.TaskAssignedEngineEvent
import io.holunda.camunda.taskpool.api.task.TaskCompletedEngineEvent
import io.holunda.camunda.taskpool.api.task.TaskCreatedEngineEvent
import io.holunda.polyflow.view.Task
import io.holunda.polyflow.view.TaskWithDataEntries
import io.holunda.polyflow.view.auth.User
import io.holunda.polyflow.view.jpa.itest.TestApplication
import io.holunda.polyflow.view.jpa.process.toSourceReference
import io.holunda.polyflow.view.query.task.*
import org.assertj.core.api.Assertions.assertThat
import org.axonframework.messaging.MetaData
import org.axonframework.queryhandling.GenericSubscriptionQueryUpdateMessage
import org.axonframework.queryhandling.QueryUpdateEmitter
import org.axonframework.queryhandling.SubscriptionQueryUpdateMessage
import org.camunda.bpm.engine.variable.Variables.createVariables
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.atLeast
import org.mockito.kotlin.clearInvocations
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.*
import java.util.function.Predicate

@RunWith(SpringRunner::class)
@SpringBootTest(
  classes = [TestApplication::class],
  properties = [
    "polyflow.view.jpa.stored-items=TASK"
  ]
)
@ActiveProfiles("itest", "mock-query-emitter")
@Transactional
internal class JpaPolyflowViewServiceTaskITest {

  private val emittedQueryUpdates: MutableList<QueryUpdate<Any>> = mutableListOf()

  @Autowired
  lateinit var queryUpdateEmitter: QueryUpdateEmitter

  @Autowired
  lateinit var jpaPolyflowViewService: JpaPolyflowViewTaskService

  @Autowired
  lateinit var dbCleaner: DbCleaner

  @Autowired
  lateinit var objectMapper: ObjectMapper

  private val id = UUID.randomUUID().toString()
  private val id2 = UUID.randomUUID().toString()
  private val now = Instant.now()

  @Before
  fun `ingest events`() {
    val payload = mapOf(
      "key" to "value",
      "key-int" to 1,
      "complex" to Pojo(
        attribute1 = "value",
        attribute2 = Date.from(now)
      )
    )

    jpaPolyflowViewService.on(
      event = TaskCreatedEngineEvent(
        id = id,
        taskDefinitionKey = "task.def.0815",
        name = "task name 1",
        priority = 50,
        sourceReference = processReference().toSourceReference(),
        payload = createVariables().apply { putAll(payload) },
        businessKey = "business-1",
        createTime = Date.from(now),
        candidateUsers = setOf("kermit"),
        candidateGroups = setOf("muppets")
      ), metaData = MetaData.emptyInstance()
    )

    jpaPolyflowViewService.on(
      event = TaskAssignedEngineEvent(
        id = id,
        taskDefinitionKey = "task.def.0815",
        name = "task name 1",
        priority = 25,
        sourceReference = processReference().toSourceReference(),
        payload = createVariables().apply { putAll(payload) },
        businessKey = "business-1",
        createTime = Date.from(now),
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
        payload = createVariables().apply { putAll(payload) },
        businessKey = "business-2",
        createTime = Date.from(now),
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
        payload = createVariables().apply { putAll(payload) },
        businessKey = "business-2",
        createTime = Date.from(now),
        assignee = "piggy",
        candidateUsers = setOf("piggy"),
        candidateGroups = setOf("muppets")
      ), metaData = MetaData.emptyInstance()
    )

  }

  @After
  fun `cleanup projection`() {
    dbCleaner.cleanup()
    // clear updates
    emittedQueryUpdates.clear()
    clearInvocations(queryUpdateEmitter)
  }

  @Test
  fun `should find the task by id`() {
    val byId1 = jpaPolyflowViewService.query(TaskForIdQuery(id = id))
    assertThat(byId1).isNotNull
    assertThat(byId1!!.id).isEqualTo(id)
  }

  @Test
  fun `should find the task with data entries by id`() {
    val byId1 = jpaPolyflowViewService.query(TaskWithDataEntriesForIdQuery(id = id))
    assertThat(byId1).isNotNull
    assertThat(byId1!!.task.id).isEqualTo(id)
  }

  @Test
  fun `should find the task by user`() {
    val kermit = jpaPolyflowViewService.query(TasksForUserQuery(user = User("kermit", setOf())))
    assertThat(kermit.elements).isNotEmpty
    assertThat(kermit.elements[0].id).isEqualTo(id)
    assertThat(kermit.elements[0].name).isEqualTo("task name 1")
    val muppets = jpaPolyflowViewService.query(TasksForUserQuery(user = User("other", setOf("muppets"))))
    assertThat(muppets.elements).isNotEmpty
    assertThat(muppets.elements[0].id).isEqualTo(id)
  }

  @Test
  fun `query updates are sent`() {
    captureEmittedQueryUpdates()
    assertThat(emittedQueryUpdates).hasSize(20)

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
      it.queryType == TasksWithDataEntriesForUserQuery::class.java && it.asTaskWithDataEntriesQueryResult().elements.map { taskW -> taskW.task.id }.contains(id2)
    }).hasSize(2)

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

}
