package io.holunda.camunda.taskpool.core.process

import io.holunda.camunda.taskpool.api.process.instance.*
import io.holunda.camunda.taskpool.api.task.ProcessReference
import org.axonframework.test.aggregate.AggregateTestFixture
import org.junit.Test

class ProcessInstanceAggregateTest {

  private val fixture = AggregateTestFixture(ProcessInstanceAggregate::class.java)
  private val sourceReference = ProcessReference(
    "id:1",
    "id:1",
    "process-definition-key:67",
    "process-definition-key",
    "My Process",
    "My application",
    null
  )


  @Test
  fun `should create aggregate on instance start`() {

    val command = StartProcessInstanceCommand(
      "id:1",
      sourceReference,
      "businessKey-4711",
      "kermit",
      null,
      "start_event"
    )

    fixture
      .givenNoPriorActivity()
      .`when`(command)
      .expectEvents(ProcessInstanceStartedEvent(
        command.processInstanceId,
        command.sourceReference,
        command.businessKey,
        command.startUserId,
        command.superInstanceId,
        command.startActivityId
      ))
  }

  @Test
  fun `should react on instance suspend`() {

    val command = SuspendProcessInstanceCommand(
      "id:1",
      sourceReference,
    )

    fixture
      .given(
        ProcessInstanceStartedEvent(
          "id:1",
          sourceReference,
          "businessKey-4711",
          "kermit",
          null,
          "start_event"
        )
      )
      .`when`(command)
      .expectEvents(ProcessInstanceSuspendedEvent(
        command.processInstanceId,
        command.sourceReference,
      ))
  }


  @Test
  fun `should react on instance resume`() {

    val command = ResumeProcessInstanceCommand(
      "id:1",
      sourceReference
    )

    fixture
      .given(
        ProcessInstanceStartedEvent(
          "id:1",
          sourceReference,
          "businessKey-4711",
          "kermit",
          null,
          "start_event"
        ),
        ProcessInstanceResumedEvent(
          command.processInstanceId,
          command.sourceReference,
        )
      )
      .`when`(command)
      .expectEvents(ProcessInstanceResumedEvent(
        command.processInstanceId,
        command.sourceReference,
      ))
  }


  @Test
  fun `should react on instance finish`() {

    val command = FinishProcessInstanceCommand(
      "id:1",
      sourceReference,
      "businessKey-4711",
      "kermit",
      "end_event"
    )

    fixture
      .given(
        ProcessInstanceStartedEvent(
          "id:1",
          sourceReference,
          "businessKey-4711",
          "kermit",
          null,
          "start_event"
        )
      )
      .`when`(command)
      .expectEvents(ProcessInstanceEndedEvent(
        command.processInstanceId,
        command.sourceReference,
        command.businessKey,
        command.superInstanceId,
        command.endActivityId
      ))
  }

  @Test
  fun `should react on instance cancel`() {

    val command = CancelProcessInstanceCommand(
      "id:1",
      sourceReference,
      "businessKey-4711",
      "kermit",
      "end_event",
      "Killed in cockpit"
    )

    fixture
      .given(
        ProcessInstanceStartedEvent(
          "id:1",
          sourceReference,
          "businessKey-4711",
          "kermit",
          null,
          "start_event"
        )
      )
      .`when`(command)
      .expectEvents(ProcessInstanceCancelledEvent(
        command.processInstanceId,
        command.sourceReference,
        command.businessKey,
        command.superInstanceId,
        command.endActivityId,
        command.deleteReason
      ))
  }

}
