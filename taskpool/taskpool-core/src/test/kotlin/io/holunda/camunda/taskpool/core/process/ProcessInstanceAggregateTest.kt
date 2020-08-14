package io.holunda.camunda.taskpool.core.process

import io.holunda.camunda.taskpool.api.process.instance.EndProcessInstanceCommand
import io.holunda.camunda.taskpool.api.process.instance.ProcessInstanceEndedEvent
import io.holunda.camunda.taskpool.api.process.instance.ProcessInstanceStartedEvent
import io.holunda.camunda.taskpool.api.process.instance.StartProcessInstanceCommand
import io.holunda.camunda.taskpool.api.task.ProcessReference
import org.axonframework.test.aggregate.AggregateTestFixture
import org.junit.Test

class ProcessInstanceAggregateTest {

  val fixture = AggregateTestFixture<ProcessInstanceAggregate>(ProcessInstanceAggregate::class.java)


  @Test
  fun `should create aggregate on instance start`() {

    val command = StartProcessInstanceCommand(
      "id:1",
      ProcessReference(
        "id:1",
        "id:1",
        "process-definition-key:67",
        "process-definition-key",
        "My Process",
        "My application",
        null
      ),
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
  fun `should react on instance end`() {

    val command = EndProcessInstanceCommand(
      "id:1",
      ProcessReference(
        "id:1",
        "id:1",
        "process-definition-key:67",
        "process-definition-key",
        "My Process",
        "My application",
        null
      ),
      "businessKey-4711",
      "kermit",
      "end_event",
      "Killed in cockpit"
    )

    fixture
      .given(
        ProcessInstanceStartedEvent(
          "id:1",
          ProcessReference(
            "id:1",
            "id:1",
            "process-definition-key:67",
            "process-definition-key",
            "My Process",
            "My application",
            null
          ),
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
        command.endActivityId,
        command.deleteReason
      ))
  }

}
