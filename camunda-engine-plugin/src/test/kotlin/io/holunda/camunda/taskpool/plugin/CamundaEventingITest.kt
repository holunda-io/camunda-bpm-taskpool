package io.holunda.camunda.taskpool.plugin

import mu.KLogging
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.fail
import org.camunda.bpm.engine.RuntimeService
import org.camunda.bpm.engine.TaskService
import org.camunda.bpm.engine.impl.history.event.HistoricIdentityLinkLogEventEntity
import org.camunda.bpm.engine.impl.history.event.HistoricTaskInstanceEventEntity
import org.camunda.bpm.engine.impl.history.event.HistoryEvent
import org.camunda.bpm.engine.runtime.ProcessInstance
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner
import java.time.LocalDate.now
import java.util.*

@RunWith(SpringRunner::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("itest")
open class CamundaEventingITest {

  companion object : KLogging()

  @Autowired
  private lateinit var properties: CamundaEventingProperties

  @Autowired
  private lateinit var runtime: RuntimeService

  @Autowired
  private lateinit var taskService: TaskService

  @Autowired
  private lateinit var eventCaptor: TestEventCaptor

  private lateinit var instance: ProcessInstance

  @Before
  fun init() {
    logger.info { "Initialized $properties" }
    eventCaptor.events.clear()
    instance = runtime.startProcessInstanceByKey("test")
    logger.info { "Started instance $instance" }
  }


  @After
  fun stop() {
    runtime.deleteProcessInstance(instance.processInstanceId, "test shutdown")
  }

  @Test
  fun `should event task assignment changes`() {

    // given
    assertThat(eventCaptor.events).isNotEmpty
    eventCaptor.events.clear()
    assertThat(eventCaptor.events).isEmpty()

    val task = taskService.createTaskQuery().active().singleResult()

    // when
    taskService.addCandidateUser(task.id, "userId")
    taskService.addCandidateGroup(task.id, "groupId")
    taskService.deleteCandidateUser(task.id, "userId")
    taskService.deleteCandidateGroup(task.id, "groupId")

    // then in reverse order

    // Remove candidate group
    var candidateGroupEvent = eventCaptor.events.pop()
    assertThat(candidateGroupEvent.eventType).isEqualTo("delete-identity-link")
    if (candidateGroupEvent is HistoricIdentityLinkLogEventEntity) {
      assertThat(candidateGroupEvent.type).isEqualTo("candidate")
      assertThat(candidateGroupEvent.operationType).isEqualTo("delete")
      assertThat(candidateGroupEvent.groupId).isEqualTo("groupId")
    } else {
      fail("Expected identity link log event")
    }


    // Remove candidate user
    var candidateUserEvent = eventCaptor.events.pop()
    assertThat(candidateUserEvent.eventType).isEqualTo("delete-identity-link")
    if (candidateUserEvent is HistoricIdentityLinkLogEventEntity) {
      assertThat(candidateUserEvent.type).isEqualTo("candidate")
      assertThat(candidateUserEvent.operationType).isEqualTo("delete")
      assertThat(candidateUserEvent.userId).isEqualTo("userId")
    } else {
      fail("Expected identity link log event")
    }

    // Add candidate group
    candidateGroupEvent = eventCaptor.events.pop()
    assertThat(candidateGroupEvent.eventType).isEqualTo("add-identity-link")
    if (candidateGroupEvent is HistoricIdentityLinkLogEventEntity) {
      assertThat(candidateGroupEvent.type).isEqualTo("candidate")
      assertThat(candidateGroupEvent.operationType).isEqualTo("add")
      assertThat(candidateGroupEvent.groupId).isEqualTo("groupId")
    } else {
      fail("Expected identity link log event")
    }

    // Add candidate user
    candidateUserEvent = eventCaptor.events.pop()
    assertThat(candidateUserEvent.eventType).isEqualTo("add-identity-link")
    if (candidateUserEvent is HistoricIdentityLinkLogEventEntity) {
      assertThat(candidateUserEvent.type).isEqualTo("candidate")
      assertThat(candidateUserEvent.operationType).isEqualTo("add")
      assertThat(candidateUserEvent.userId).isEqualTo("userId")
    } else {
      fail("Expected identity link log event")
    }

    assertThat(eventCaptor.events).isEmpty()
  }

  @Test
  fun `should event task attribute changes`() {
    assertThat(eventCaptor.events).isNotEmpty
    eventCaptor.events.clear()

    val task = taskService.createTaskQuery().active().singleResult()

    task.name = "new Name"
    taskService.saveTask(task)


    var taskChangeEvent = eventCaptor.events.pop()
    assertThat(taskChangeEvent.eventType).isEqualTo("update")
    if (taskChangeEvent is HistoricTaskInstanceEventEntity) {
      assertThat(taskChangeEvent.name).isEqualTo("new Name")
    } else {
      fail("Expected task instance change event")
    }

  }

  @Test
  fun `should event task multiple assignment changes`() {

    // given
    assertThat(eventCaptor.events).isNotEmpty
    eventCaptor.events.clear()
    assertThat(eventCaptor.events).isEmpty()

    val task = taskService.createTaskQuery().active().singleResult()

    // when
    taskService.addCandidateUser(task.id, "user1")
    taskService.addCandidateUser(task.id, "user2")

    // then in reverse order

    // Add candidate user
    var candidateUserEvent = eventCaptor.events.pop()
    assertThat(candidateUserEvent.eventType).isEqualTo("add-identity-link")
    if (candidateUserEvent is HistoricIdentityLinkLogEventEntity) {
      assertThat(candidateUserEvent.type).isEqualTo("candidate")
      assertThat(candidateUserEvent.operationType).isEqualTo("add")
      assertThat(candidateUserEvent.userId).isEqualTo("user2")
    } else {
      fail("Expected identity link log event")
    }

    // Add candidate user
    candidateUserEvent = eventCaptor.events.pop()
    assertThat(candidateUserEvent.eventType).isEqualTo("add-identity-link")
    if (candidateUserEvent is HistoricIdentityLinkLogEventEntity) {
      assertThat(candidateUserEvent.type).isEqualTo("candidate")
      assertThat(candidateUserEvent.operationType).isEqualTo("add")
      assertThat(candidateUserEvent.userId).isEqualTo("user1")
    } else {
      fail("Expected identity link log event")
    }

    assertThat(eventCaptor.events).isEmpty()
  }

    @Test
  fun `should event task follow-up data changes`() {
    assertThat(eventCaptor.events).isNotEmpty
    eventCaptor.events.clear()

    val task = taskService.createTaskQuery().active().singleResult()

    val now = Date()

    task.followUpDate = now
    taskService.saveTask(task)


    var taskChangeEvent = eventCaptor.events.pop()
    assertThat(taskChangeEvent.eventType).isEqualTo("update")
    if (taskChangeEvent is HistoricTaskInstanceEventEntity) {
      assertThat(taskChangeEvent.followUpDate).isEqualTo(now)
    } else {
      fail("Expected task instance change event")
    }

  }


}

@Component
class TestEventCaptor {

  val events = Stack<HistoryEvent>()

  @EventListener
  fun onEvent(e: HistoryEvent) {
    events.push(e)
  }
}
