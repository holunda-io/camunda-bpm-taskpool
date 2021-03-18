package task

import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import io.holunda.camunda.client.task.TaskEventHandlers
import io.holunda.camunda.taskpool.TaskCollectorProperties
import io.holunda.camunda.taskpool.api.task.*
import org.camunda.bpm.engine.TaskService
import org.camunda.bpm.extension.mockito.QueryMocks
import org.camunda.bpm.extension.mockito.task.TaskFake
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import java.util.*

@ExtendWith(MockitoExtension::class)
class TaskEventHandlerTest {

  private val taskCollectorProperties = TaskCollectorProperties(applicationName = "myApplication")
  private val processReference = ProcessReference(
    instanceId = UUID.randomUUID().toString(),
    name = "My Process",
    applicationName = taskCollectorProperties.applicationName,
    definitionId = "PROCESS:001",
    definitionKey = "PROCESS",
    executionId = UUID.randomUUID().toString()
  )


  @Mock
  private lateinit var taskService: TaskService
  private lateinit var taskEventHandlers: TaskEventHandlers
  private lateinit var now: Date

  @BeforeEach
  fun init() {
    taskEventHandlers = TaskEventHandlers(taskService, taskCollectorProperties)
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

    val taskFake = TaskFake.builder().id("4711").build()
    QueryMocks.mockTaskQuery(taskService).singleResult(taskFake)

    taskEventHandlers.on(TaskClaimedEvent(id = taskFake.id, taskDefinitionKey = "TASK-001", sourceReference = processReference, assignee = "kermit", formKey = null))

    verify(taskService).createTaskQuery()
    verify(taskService).setAssignee("4711", "kermit")

    verifyNoMoreInteractions(taskService)
  }

}
