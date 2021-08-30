package io.holunda.polyflow.view.jpa

import io.holunda.camunda.taskpool.api.process.instance.*
import io.holunda.camunda.taskpool.api.task.ProcessReference
import io.holunda.camunda.taskpool.api.task.SourceReference
import io.holunda.polyflow.view.ProcessInstanceState
import io.holunda.polyflow.view.jpa.itest.TestApplication
import io.holunda.polyflow.view.query.process.ProcessInstanceQueryResult
import io.holunda.polyflow.view.query.process.ProcessInstancesByStateQuery
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner
import java.util.*
import javax.transaction.Transactional

@RunWith(SpringRunner::class)
@SpringBootTest(classes = [TestApplication::class])
@ActiveProfiles("itest", "mock-query-emitter")
@Transactional
internal class JpaPolyflowViewServiceProcessInstanceITest {

  @Autowired
  lateinit var jpaPolyflowViewService: JpaPolyflowViewProcessInstanceService

  @Autowired
  lateinit var dbCleaner: DbCleaner

  private val instanceId = UUID.randomUUID().toString()
  private lateinit var source: SourceReference

  @Before
  fun `ingest events`() {

    source = ProcessReference(
      instanceId = instanceId,
      executionId = UUID.randomUUID().toString(),
      definitionId = "my-process-def-key:13",
      definitionKey = "my-process-def-key",
      name = "My process",
      applicationName = "test-application",
      tenantId = null
    )

    jpaPolyflowViewService.on(
      event = ProcessInstanceStartedEvent(
        processInstanceId = instanceId,
        businessKey = "businessKey",
        superInstanceId = null,
        startActivityId = "start",
        startUserId = "kermit",
        sourceReference = source
      )
    )

  }

  @After
  fun `cleanup projection`() {
    dbCleaner.cleanup()
  }

  @Test
  fun `should find process instance by state running`() {
    val result = jpaPolyflowViewService.query(
      ProcessInstancesByStateQuery(states = setOf(ProcessInstanceState.RUNNING))
    )
    assertThat(result.payload).isNotNull
    val response: ProcessInstanceQueryResult = result.payload
    assertThat(response.elements).isNotEmpty
    assertThat(response.elements[0].processInstanceId).isEqualTo(instanceId)
    assertThat(response.elements[0].state).isEqualTo(ProcessInstanceState.RUNNING)
    assertThat(response.elements[0].businessKey).isEqualTo("businessKey")
    assertThat(response.elements[0].superInstanceId).isNull()
    assertThat(response.elements[0].startActivityId).isEqualTo("start")
    assertThat(response.elements[0].startUserId).isEqualTo("kermit")
    assertThat(response.elements[0].sourceReference).isEqualTo(source)
    assertThat(response.elements[0].endActivityId).isNull()
    assertThat(response.elements[0].deleteReason).isNull()
  }

  @Test
  fun `should find process instance by state suspended`() {

    jpaPolyflowViewService.on(
      ProcessInstanceSuspendedEvent(
        processInstanceId = instanceId,
        sourceReference = source
      )
    )

    assertThat(
      jpaPolyflowViewService.query(
        ProcessInstancesByStateQuery(states = setOf(ProcessInstanceState.RUNNING))
      ).payload.elements
    ).isEmpty()

    val result = jpaPolyflowViewService.query(
      ProcessInstancesByStateQuery(states = setOf(ProcessInstanceState.SUSPENDED))
    )
    assertThat(result.payload).isNotNull
    val response: ProcessInstanceQueryResult = result.payload
    assertThat(response.elements).isNotEmpty
    assertThat(response.elements[0].processInstanceId).isEqualTo(instanceId)
    assertThat(response.elements[0].state).isEqualTo(ProcessInstanceState.SUSPENDED)
    assertThat(response.elements[0].businessKey).isEqualTo("businessKey")
    assertThat(response.elements[0].superInstanceId).isNull()
    assertThat(response.elements[0].startActivityId).isEqualTo("start")
    assertThat(response.elements[0].startUserId).isEqualTo("kermit")
    assertThat(response.elements[0].sourceReference).isEqualTo(source)
    assertThat(response.elements[0].endActivityId).isNull()
    assertThat(response.elements[0].deleteReason).isNull()

    jpaPolyflowViewService.on(
      ProcessInstanceResumedEvent(
        processInstanceId = instanceId,
        sourceReference = source
      )
    )

    val runningAgain = jpaPolyflowViewService.query(
      ProcessInstancesByStateQuery(states = setOf(ProcessInstanceState.RUNNING))
    )
    assertThat(runningAgain.payload).isNotNull
    val responseRunning: ProcessInstanceQueryResult = runningAgain.payload
    assertThat(responseRunning.elements).isNotEmpty
    assertThat(responseRunning.elements[0].processInstanceId).isEqualTo(instanceId)
    assertThat(responseRunning.elements[0].state).isEqualTo(ProcessInstanceState.RUNNING)
    assertThat(responseRunning.elements[0].businessKey).isEqualTo("businessKey")
    assertThat(responseRunning.elements[0].superInstanceId).isNull()
    assertThat(responseRunning.elements[0].startActivityId).isEqualTo("start")
    assertThat(responseRunning.elements[0].startUserId).isEqualTo("kermit")
    assertThat(responseRunning.elements[0].sourceReference).isEqualTo(source)
    assertThat(responseRunning.elements[0].endActivityId).isNull()
    assertThat(responseRunning.elements[0].deleteReason).isNull()

  }


  @Test
  fun `should find process instance by state cancelled`() {

    jpaPolyflowViewService.on(
      ProcessInstanceCancelledEvent(
        processInstanceId = instanceId,
        sourceReference = source,
        businessKey = "new-key",
        deleteReason = "deleted",
        endActivityId = "intermediate"
      )
    )

    assertThat(
      jpaPolyflowViewService.query(
        ProcessInstancesByStateQuery(states = setOf(ProcessInstanceState.RUNNING))
      ).payload.elements
    ).isEmpty()

    val result = jpaPolyflowViewService.query(
      ProcessInstancesByStateQuery(states = setOf(ProcessInstanceState.CANCELLED))
    )
    assertThat(result.payload).isNotNull
    val response: ProcessInstanceQueryResult = result.payload
    assertThat(response.elements).isNotEmpty
    assertThat(response.elements[0].processInstanceId).isEqualTo(instanceId)
    assertThat(response.elements[0].state).isEqualTo(ProcessInstanceState.CANCELLED)
    assertThat(response.elements[0].businessKey).isEqualTo("businessKey")
    assertThat(response.elements[0].superInstanceId).isNull()
    assertThat(response.elements[0].startActivityId).isEqualTo("start")
    assertThat(response.elements[0].startUserId).isEqualTo("kermit")
    assertThat(response.elements[0].sourceReference).isEqualTo(source)
    assertThat(response.elements[0].endActivityId).isEqualTo("intermediate")
    assertThat(response.elements[0].deleteReason).isEqualTo("deleted")
  }

  @Test
  fun `should find process instance by state deleted`() {

    jpaPolyflowViewService.on(
      ProcessInstanceEndedEvent(
        processInstanceId = instanceId,
        sourceReference = source,
        businessKey = "new-key",
        endActivityId = "end"
      )
    )

    val result = jpaPolyflowViewService.query(
      ProcessInstancesByStateQuery(states = setOf(ProcessInstanceState.FINISHED))
    )
    assertThat(result.payload).isNotNull
    val response: ProcessInstanceQueryResult = result.payload
    assertThat(response.elements).isNotEmpty
    assertThat(response.elements[0].processInstanceId).isEqualTo(instanceId)
    assertThat(response.elements[0].state).isEqualTo(ProcessInstanceState.FINISHED)
    assertThat(response.elements[0].businessKey).isEqualTo("businessKey")
    assertThat(response.elements[0].superInstanceId).isNull()
    assertThat(response.elements[0].startActivityId).isEqualTo("start")
    assertThat(response.elements[0].startUserId).isEqualTo("kermit")
    assertThat(response.elements[0].sourceReference).isEqualTo(source)
    assertThat(response.elements[0].endActivityId).isEqualTo("end")
    assertThat(response.elements[0].deleteReason).isNull()
  }

}
