package io.holunda.camunda.taskpool.view.mongo.service

import com.nhaarman.mockitokotlin2.*
import io.holunda.camunda.taskpool.api.task.ProcessReference
import io.holunda.camunda.taskpool.api.task.TaskAssignedEngineEvent
import io.holunda.camunda.taskpool.view.mongo.ChangeTrackingMode
import io.holunda.camunda.taskpool.view.mongo.TaskPoolMongoViewProperties
import io.holunda.camunda.taskpool.view.mongo.repository.ProcessReferenceDocument
import io.holunda.camunda.taskpool.view.mongo.repository.TaskDocument
import io.holunda.camunda.taskpool.view.mongo.repository.TaskRepository
import org.junit.Test
import reactor.core.publisher.Mono
import java.util.*


class TaskPoolMongoServiceRetryTest {
  private val taskRepository: TaskRepository = mock()

  private val taskPoolMongoService: TaskPoolMongoService = TaskPoolMongoService(
    TaskPoolMongoViewProperties(changeTrackingMode = ChangeTrackingMode.CHANGE_STREAM),
    taskRepository,
    mock(),
    mock(),
    mock(),
    mock(),
    mock())

  private val taskId = "some-id"

  private val processReference = ProcessReference("", "", "", "", "", "")

  @Test
  fun `retries updates if task is not yet present in database`() {
    val taskDocument = TaskDocument(taskId, ProcessReferenceDocument(processReference), "foo:bar")
    val results = ArrayDeque<Mono<TaskDocument>>(listOf(
      Mono.empty(),
      Mono.empty(),
      Mono.just(taskDocument)
    ))
    whenever(taskRepository.findNotDeletedById(taskId)).thenReturn(Mono.defer { results.poll() })
    whenever(taskRepository.save(any<TaskDocument>())).thenAnswer { Mono.just(it.getArgument<TaskDocument>(0)) }
    taskPoolMongoService.on(TaskAssignedEngineEvent(taskId, processReference, "foo:bar"))
    verify(taskRepository).save(taskDocument)
  }

  @Test
  fun `stops retrying after five attempts`() {
    whenever(taskRepository.findNotDeletedById(taskId)).thenReturn(Mono.empty())
    taskPoolMongoService.on(TaskAssignedEngineEvent(taskId, processReference, "foo:bar"))
    verify(taskRepository, never()).save(any<TaskDocument>())
  }
}
