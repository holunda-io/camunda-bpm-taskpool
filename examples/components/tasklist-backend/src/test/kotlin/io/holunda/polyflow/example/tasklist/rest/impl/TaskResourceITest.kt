package io.holunda.polyflow.example.tasklist.rest.impl

import com.fasterxml.jackson.databind.ObjectMapper
import io.holunda.camunda.taskpool.api.task.*
import io.holunda.polyflow.example.tasklist.rest.Rest.REQUEST_PATH
import io.holunda.polyflow.example.tasklist.itest.ITestApplication
import io.holunda.polyflow.example.tasklist.itest.ITestApplication.Companion.ITEST
import io.holunda.polyflow.view.Task
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset.UTC
import java.util.*


@RunWith(SpringRunner::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = [ITestApplication::class])
@ActiveProfiles(ITEST)
internal class TaskResourceITest {

  private lateinit var mockMvc: MockMvc

  @MockBean
  private lateinit var taskServiceGateway: TaskServiceGateway

  @Autowired
  private lateinit var webApplicationContext: WebApplicationContext

  @Autowired
  private lateinit var resource: TaskResource

  @Autowired
  private lateinit var objectMapper: ObjectMapper

  @Before
  fun setup() {
    this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build()
  }

  @Test
  fun `should fallback to current user service if no user is passed and return forbidden if the user is not found`() {
    val taskId = UUID.randomUUID().toString()

    whenever(taskServiceGateway.getTask(any())).thenReturn(testTask(taskId))

    this.mockMvc
      .perform(
        post("${REQUEST_PATH}/task/{id}/claim", taskId)
          .servletPath(REQUEST_PATH)
      )
      .andExpect(status().isForbidden)
  }

  @Test
  fun `should not find an existing task if the user is not authorized`() {
    val taskId = UUID.randomUUID().toString()

    whenever(taskServiceGateway.getTask(any())).thenReturn(testTask(taskId))

    this.mockMvc
      .perform(
        post("${REQUEST_PATH}/task/{id}/claim", taskId)
          .header("X-Current-User-ID", "id1")
          .servletPath(REQUEST_PATH)
      )
      .andExpect(status().isNotFound)
  }


  @Test
  fun `should claim task`() {

    val taskId = UUID.randomUUID().toString()
    val task = testTask(taskId, candidateUsers = setOf("kermit"))

    whenever(taskServiceGateway.getTask(any())).thenReturn(task)

    this.mockMvc
      .perform(
        post("${REQUEST_PATH}/task/{id}/claim", taskId)
          .header("X-Current-User-ID", "id1")
          .servletPath(REQUEST_PATH)
      )
      .andExpect(status().isNoContent)

    verify(taskServiceGateway).send(ClaimInteractionTaskCommand(taskId, task.sourceReference, task.taskDefinitionKey, "kermit"))
  }

  @Test
  fun `should unclaim task`() {

    val taskId = UUID.randomUUID().toString()
    val task = testTask(taskId, assignee = "kermit")

    whenever(taskServiceGateway.getTask(any())).thenReturn(task)

    this.mockMvc
      .perform(
        post("${REQUEST_PATH}/task/{id}/unclaim", taskId)
          .header("X-Current-User-ID", "id1")
          .servletPath(REQUEST_PATH)
      )
      .andExpect(status().isNoContent)

    verify(taskServiceGateway).send(UnclaimInteractionTaskCommand(taskId, task.sourceReference, task.taskDefinitionKey))
  }

  @Test
  fun `should defer task`() {

    val date = Instant.now()
    val taskId = UUID.randomUUID().toString()
    val task = testTask(taskId, assignee = "kermit")
    val json = objectMapper.writeValueAsString(OffsetDateTime.ofInstant(date, UTC))

    whenever(taskServiceGateway.getTask(any())).thenReturn(task)

    this.mockMvc
      .perform(
        post("${REQUEST_PATH}/task/{id}/defer", taskId)
          .header("X-Current-User-ID", "id1")
          .servletPath(REQUEST_PATH)
          .contentType(MediaType.APPLICATION_JSON)
          .content(json)
      )
      .andExpect(status().isNoContent)

    verify(taskServiceGateway).send(DeferInteractionTaskCommand(taskId, task.sourceReference, task.taskDefinitionKey, Date.from(date)))
  }

  @Test
  fun `should undefer task`() {

    val taskId = UUID.randomUUID().toString()
    val task = testTask(taskId, assignee = "kermit")

    whenever(taskServiceGateway.getTask(any())).thenReturn(task)

    this.mockMvc
      .perform(
        post("${REQUEST_PATH}/task/{id}/undefer", taskId)
          .header("X-Current-User-ID", "id1")
          .servletPath(REQUEST_PATH)
      )
      .andExpect(status().isNoContent)

    verify(taskServiceGateway).send(UndeferInteractionTaskCommand(taskId, task.sourceReference, task.taskDefinitionKey))
  }

  fun testTask(
    id: String = UUID.randomUUID().toString(),
    assignee: String? = null,
    candidateUsers: Set<String> = setOf(),
    candidateGroups: Set<String> = setOf()
  ) = Task(
    id = id,
    sourceReference = ProcessReference(
      instanceId = "instaneId",
      executionId = "executionId",
      definitionId = "process-def:1",
      definitionKey = "process-def",
      name = "My process",
      applicationName = "appName",
    ),
    taskDefinitionKey = "task1",
    assignee = assignee,
    candidateUsers = candidateUsers,
    candidateGroups = candidateGroups
  )
}
