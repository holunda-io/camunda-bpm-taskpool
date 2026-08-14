package io.holunda.polyflow.client.processengineapi.task

import dev.bpmcrafters.processengineapi.task.ChangeAssignmentModifyTaskCmd
import dev.bpmcrafters.processengineapi.task.ModifyTaskCmd
import dev.bpmcrafters.processengineapi.task.TaskInformation
import dev.bpmcrafters.processengineapi.task.UserTaskCompletionApi
import dev.bpmcrafters.processengineapi.task.UserTaskModificationApi
import dev.bpmcrafters.processengineapi.task.support.UserTaskSupport
import io.holunda.camunda.taskpool.api.task.*
import io.holunda.polyflow.client.processengineapi.EngineClientProperties
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever
import java.util.*

@ExtendWith(MockitoExtension::class)
class TaskEventHandlerTest {

  private val properties = EngineClientProperties(applicationName = "myApplication")
  private val processReference = ProcessReference(
    instanceId = UUID.randomUUID().toString(),
    name = "My Process",
    applicationName = properties.applicationName,
    definitionId = "PROCESS:001",
    definitionKey = "PROCESS",
    executionId = UUID.randomUUID().toString()
  )





  @Mock private lateinit var  taskService: UserTaskModificationApi
  @Mock private lateinit var  taskCompletionService: UserTaskCompletionApi
  @Mock private lateinit var  taskQuery: UserTaskSupport

  private lateinit var taskEventHandlers: TaskEventHandlers
  private lateinit var now: Date

  @BeforeEach
  fun init() {
    taskEventHandlers = TaskEventHandlers(
      taskService = taskService,
      taskCompletionService = taskCompletionService,
      taskQuery = taskQuery,
      properties = properties
    )
    now = Date()
  }


  @Test
  fun `should ignore event if not addressed to current application`() {

    val otherReference = ProcessReference(
      instanceId = UUID.randomUUID().toString(),
      name = "My Process",
      applicationName = "another application",
      definitionId = "PROCESS:001",
      definitionKey = "PROCESS",
      executionId = UUID.randomUUID().toString()
    )

    taskEventHandlers.on(TaskClaimedEvent(id = "4711", taskDefinitionKey = "TASK-001", sourceReference = otherReference, assignee = "kermit", formKey = null))
    taskEventHandlers.on(TaskUnclaimedEvent(id = "4711", taskDefinitionKey = "TASK-001", sourceReference = otherReference, formKey = null))
    taskEventHandlers.on(TaskDeferredEvent(id = "4711", taskDefinitionKey = "TASK-001", sourceReference = otherReference, followUpDate = now, formKey = null))
    taskEventHandlers.on(TaskUndeferredEvent(id = "4711", taskDefinitionKey = "TASK-001", sourceReference = otherReference, formKey = null))
    taskEventHandlers.on(TaskToBeCompletedEvent(id = "4711", taskDefinitionKey = "TASK-001", sourceReference = otherReference, formKey = null))

    verifyNoMoreInteractions(taskService)
  }

  @Test
  fun `should claim`() {
    val taskFake = TaskInformation(taskId =  "4711", meta = mapOf())
    whenever(taskQuery.exists("4711")).thenReturn(true)

    taskEventHandlers.on(
      TaskClaimedEvent(
        id = taskFake.taskId,
        taskDefinitionKey = "TASK-001",
        sourceReference = processReference,
        assignee = "kermit",
        formKey = null
      )
    )

    val argumentCaptor = argumentCaptor<ModifyTaskCmd>()
    verify(taskService).update(argumentCaptor.capture())
    val modifyTaskCmd = argumentCaptor.firstValue
    assertThat(modifyTaskCmd).isInstanceOf(ChangeAssignmentModifyTaskCmd.AssignTaskCmd::class.java)
    modifyTaskCmd as ChangeAssignmentModifyTaskCmd.AssignTaskCmd
    assertThat(modifyTaskCmd.assignee).isEqualTo("kermit")

    verifyNoMoreInteractions(taskService)
  }

}
