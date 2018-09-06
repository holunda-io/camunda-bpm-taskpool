package io.holunda.camunda.taskpool.core

import io.holunda.camunda.taskpool.api.task.CreateTaskCommand
import io.holunda.camunda.taskpool.api.task.TaskCreatedEvent
import org.axonframework.test.aggregate.AggregateTestFixture
import org.axonframework.test.aggregate.FixtureConfiguration
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import java.util.*

class TaskAggregateTest {

  private lateinit var fixture: FixtureConfiguration<TaskAggregate>
  private lateinit var now: Date

  @Before
  fun setUp() {
    fixture = AggregateTestFixture<TaskAggregate>(TaskAggregate::class.java)
    now = Date()
  }

  @Ignore
  @Test
  fun `should create task`() {
    fixture
      .givenNoPriorActivity()
      .`when`(
        CreateTaskCommand(id = "4711", name = "Foo", createTime = now, eventName = "create", owner = "kermit", taskDefinitionKey = "foo"))
      .expectEvents(
        TaskCreatedEvent(
          id = "4711",
          name = "Foo", createTime = now, eventName = "create", owner = "kermit", taskDefinitionKey = "foo",
          caseReference = null, processReference = null, payload = mutableMapOf(), assignee = null, candidateGroups = listOf(),
          candidateUsers = listOf(), deleteReason = null, dueDate = null, priority = 50, description = null))

  }
}
