package io.holunda.polyflow.view.mongo.service

import com.mongodb.MongoCommandException
import com.mongodb.ServerAddress
import com.mongodb.client.model.changestream.ChangeStreamDocument
import com.mongodb.client.model.changestream.OperationType
import io.holunda.camunda.taskpool.api.task.ProcessReference
import io.holunda.polyflow.view.Task
import io.holunda.polyflow.view.mongo.ClearDeletedTasksMode
import io.holunda.polyflow.view.mongo.TaskPoolMongoViewProperties
import io.holunda.polyflow.view.mongo.data.DataEntryRepository
import io.holunda.polyflow.view.mongo.task.ProcessReferenceDocument
import io.holunda.polyflow.view.mongo.task.TaskChangeTracker
import io.holunda.polyflow.view.mongo.task.TaskDocument
import io.holunda.polyflow.view.mongo.task.TaskRepository
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.bson.BsonDocument
import org.bson.BsonString
import org.bson.Document
import org.junit.Test
import org.mockito.kotlin.*
import org.springframework.data.mongodb.core.ChangeStreamEvent
import org.springframework.data.mongodb.core.convert.MongoConverter
import org.springframework.scheduling.TaskScheduler
import org.springframework.scheduling.Trigger
import org.springframework.scheduling.support.SimpleTriggerContext
import reactor.core.Disposable
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import reactor.test.publisher.TestPublisher
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.ZoneOffset
import java.util.*

internal class TaskChangeTrackerTest {
  private val taskRepository: TaskRepository = mock()
  private val dataEntryRepository: DataEntryRepository = mock()
  private var properties = TaskPoolMongoViewProperties()
  private val scheduler: TaskScheduler = mock<TaskScheduler>().apply {
    whenever(clock).thenReturn(Clock.fixed(Instant.EPOCH, ZoneOffset.UTC))
  }

  // Lazy initialization is needed for StepVerifier.withVirtualTime to work properly
  private val taskChangeTracker by lazy { TaskChangeTracker(taskRepository, dataEntryRepository, properties, scheduler) }

  @Test
  fun `subscribes only once to mongo change stream`() {
    val changeStreamPublisher = publisherForResumeToken()

    var secondSubscription: Disposable? = null
    StepVerifier.withVirtualTime { taskChangeTracker.trackTaskUpdates() }
      .expectSubscription()
      .then { secondSubscription = taskChangeTracker.trackTaskUpdates().subscribe() }
      .then { changeStreamPublisher.assertSubscribers(1) }
      .thenCancel()
      .verify()

    changeStreamPublisher.assertSubscribers(1)
    secondSubscription!!.dispose()
    changeStreamPublisher.assertSubscribers(1)
  }

  @Test
  fun `resumes change stream at last known position after errors`() {
    val initialPublisher = publisherForResumeToken(null)
    val nextPublisher = publisherForResumeToken(1)

    StepVerifier.withVirtualTime { taskChangeTracker.trackTaskUpdates() }
      .expectSubscription()
      .then { initialPublisher.next(changeStreamEvent(1, "123")) }
      .expectNext(task("123"))
      .then { initialPublisher.error(RuntimeException("Connection down")) }
      .expectNoEvent(1.seconds)
      .then { nextPublisher.next(changeStreamEvent(2, "456")) }
      .expectNext(task("456"))
      .verifyTimeout(10.seconds)
  }

  @Test
  fun `resumes change stream at end after last subscriber cancels`() {
    val initialPublisher = publisherForResumeToken(null)

    // We're trying to test a state that should never normally happen, but we can simulate it by unsubscribing the task change tracker.
    StepVerifier.withVirtualTime { taskChangeTracker.apply { clearSubscription() }.trackTaskUpdates() }
      .expectSubscription()
      .then { initialPublisher.next(changeStreamEvent(1, "123")) }
      .expectNext(task("123"))
      .verifyTimeout(10.seconds)

    val nextPublisher = publisherForResumeToken(null)
    StepVerifier.withVirtualTime { taskChangeTracker.trackTaskUpdates() }
      .expectSubscription()
      .then { nextPublisher.next(changeStreamEvent(2, "456")) }
      .expectNext(task("456"))
      .verifyTimeout(10.seconds)
  }

  @Test
  fun `resumes change stream at end if resume token is invalid`() {
    val initialPublisher = publisherForResumeToken(null)
    val nextPublisher = publisherForResumeToken(1)
    val lastPublisher = publisher()

    StepVerifier.withVirtualTime { taskChangeTracker.trackTaskUpdates() }
      .expectSubscription()
      .then { initialPublisher.next(changeStreamEvent(1, "123")) }
      .expectNext(task("123"))
      .then { initialPublisher.error(RuntimeException("Connection down")) }
      .expectNoEvent(1.seconds)
      .then { nextPublisher.assertSubscribers() }
      .then { lastPublisher.returnForResumeToken(null) }
      .then { nextPublisher.error(MongoCommandException(BsonDocument(), ServerAddress())) }
      .expectNoEvent(1.seconds)
      .then { lastPublisher.assertSubscribers() }
      .then { lastPublisher.next(changeStreamEvent(2, "456")) }
      .expectNext(task("456"))
      .verifyTimeout(10.seconds)
  }

  @Test
  fun `deletes tasks that were marked as deleted`() {
    val initialPublisher = publisherForResumeToken(null)
    val deleteResult = TestPublisher.createCold<Void>().complete()
    whenever(taskRepository.deleteById("123")).thenReturn(deleteResult.mono())

    StepVerifier.withVirtualTime { taskChangeTracker.trackTaskUpdates() }
      .expectSubscription()
      .then { initialPublisher.next(changeStreamEvent(1, "123", deleted = true, deleteTime = Instant.EPOCH)) }
      .expectNext(task("123", deleted = true))
      .then { deleteResult.assertWasSubscribed() }
      .verifyTimeout(10.seconds)
  }

  @Test
  fun `waits before deleting tasks that were marked as deleted`() {
    properties = properties.copy(
      changeStream = properties.changeStream.copy(
        clearDeletedTasks = properties.changeStream.clearDeletedTasks.copy(
          after = Duration.ofMinutes(5)
        )
      )
    )
    val initialPublisher = publisherForResumeToken(null)
    val deleteResult = TestPublisher.createCold<Void>().complete()
    whenever(taskRepository.deleteById("123")).thenReturn(deleteResult.mono())

    StepVerifier.withVirtualTime { taskChangeTracker.trackTaskUpdates() }
      .expectSubscription()
      .then { initialPublisher.next(changeStreamEvent(1, "123", deleted = true, deleteTime = Instant.EPOCH.minus(Duration.ofMinutes(1)))) }
      .expectNext(task("123", deleted = true))
      .then { deleteResult.assertWasNotSubscribed() }
      .thenAwait(Duration.ofMinutes(4).minusNanos(1))
      .then { deleteResult.assertWasNotSubscribed() }
      .thenAwait(Duration.ofNanos(1))
      .then { deleteResult.assertWasSubscribed() }
      .verifyTimeout(10.seconds)
  }

  @Test
  fun `does not backpressure while waiting to delete tasks`() {
    properties = properties.copy(
      changeStream = properties.changeStream.copy(
        clearDeletedTasks = properties.changeStream.clearDeletedTasks.copy(
          after = Duration.ofMinutes(5),
          bufferSize = 100
        )
      )
    )
    val initialPublisher = publisherForResumeToken(null)
    val deleteResults = mutableMapOf<String, TestPublisher<Void>>()
    whenever(taskRepository.deleteById(any<String>())).thenAnswer {
      deleteResults.computeIfAbsent(it.getArgument(0)) {
        TestPublisher.createCold<Void>().complete()
      }.mono()
    }

    StepVerifier.withVirtualTime { taskChangeTracker.trackTaskUpdates() }
      .expectSubscription()
      // 100 tasks fit in the buffer, plus a few (currently 32) that fit in other buffers/queues in the Flux chain
      // All tasks exceeding this limit (currently tasks 133 to 200) are dropped and not deleted
      .then {
        (1..200).forEach {
          initialPublisher.next(
            changeStreamEvent(
              it,
              "$it",
              deleted = true,
              deleteTime = Instant.EPOCH.minus(Duration.ofMinutes(1))
            )
          )
        }
      }
      .expectNextCount(200)
      .then { deleteResults.values.forEach { it.assertWasNotSubscribed() } }
      .thenAwait(Duration.ofMinutes(4).minusNanos(1))
      .then { deleteResults.values.forEach { it.assertWasNotSubscribed() } }
      .thenAwait(Duration.ofNanos(1))
      .then {
        deleteResults.values.forEach { it.assertWasSubscribed() }
        assertThat(deleteResults).hasSizeBetween(100, 199)
      }
      .verifyTimeout(10.seconds)
  }

  @Test
  fun `deletes tasks also if previous delete failed`() {
    val initialPublisher = publisherForResumeToken(null)
    val deleteResult = TestPublisher.createCold<Void>().complete()
    whenever(taskRepository.deleteById("123")).thenReturn(Mono.error(RuntimeException("Delete failed")))
    whenever(taskRepository.deleteById("456")).thenReturn(deleteResult.mono())

    StepVerifier.withVirtualTime { taskChangeTracker.trackTaskUpdates() }
      .expectSubscription()
      .then { initialPublisher.next(changeStreamEvent(1, "123", deleted = true, deleteTime = Instant.EPOCH)) }
      .expectNext(task("123", deleted = true))
      .then { initialPublisher.next(changeStreamEvent(1, "456", deleted = true, deleteTime = Instant.EPOCH)) }
      .expectNext(task("456", deleted = true))
      .then { deleteResult.assertWasSubscribed() }
      .verifyTimeout(10.seconds)
  }

  @Test
  fun `schedules job for deleting leftover tasks`() {
    properties = properties.copy(
      changeStream = properties.changeStream.copy(
        clearDeletedTasks = properties.changeStream.clearDeletedTasks.copy(
          mode = ClearDeletedTasksMode.BOTH,
          after = Duration.ofHours(1),
          jobSchedule = "@daily",
          jobJitter = Duration.ofHours(1),
          jobTimezone = ZoneOffset.UTC
        )
      )
    )
    whenever(scheduler.clock).thenReturn(Clock.fixed(Instant.EPOCH, ZoneOffset.UTC))
    whenever(taskRepository.findDeletedBefore(Instant.EPOCH.minus(Duration.ofHours(1))))
      .thenReturn(Flux.just(taskDocument("123", deleted = true, Instant.EPOCH.minus(Duration.ofHours(2)))))
    val deleteResult = TestPublisher.createCold<Void>().complete()
    whenever(taskRepository.deleteById("123")).thenReturn(deleteResult.mono())

    val deleteRunnableCaptor = argumentCaptor<Runnable>()
    val triggerCaptor = argumentCaptor<Trigger>()
    taskChangeTracker.initCleanupJob()
    verify(scheduler).schedule(deleteRunnableCaptor.capture(), triggerCaptor.capture())
    val trigger = triggerCaptor.firstValue
    Assertions.assertThat(trigger.nextExecutionTime(SimpleTriggerContext(Clock.fixed(Instant.EPOCH, ZoneOffset.UTC)).apply {
      update(
        Date.from(Instant.EPOCH.minus(Duration.ofHours(24))),
        Date.from(Instant.EPOCH.minus(Duration.ofHours(24))),
        Date.from(Instant.EPOCH.minus(Duration.ofHours(23)))
      )
    })).isBetween(Instant.EPOCH, Instant.EPOCH.plus(Duration.ofHours(1)))
    deleteRunnableCaptor.firstValue.run()
    deleteResult.assertWasSubscribed()
  }

  private fun publisherForResumeToken(resumeToken: Int? = null): TestPublisher<ChangeStreamEvent<TaskDocument>> =
    publisher().returnForResumeToken(resumeToken)

  private fun publisher() = TestPublisher.create<ChangeStreamEvent<TaskDocument>>()

  private fun TestPublisher<ChangeStreamEvent<TaskDocument>>.returnForResumeToken(resumeToken: Int?) = apply {
    whenever(taskRepository.getTaskUpdates(resumeToken?.let { resumeToken(it) })).thenReturn(this.flux())
  }

  private fun changeStreamEvent(
    resumeToken: Int,
    taskId: String,
    deleted: Boolean = false,
    deleteTime: Instant? = if (deleted) Instant.now() else null
  ): ChangeStreamEvent<TaskDocument> {
    val converter: MongoConverter = mock()
    whenever(converter.read(eq(TaskDocument::class.java), any())).thenAnswer { (it.arguments[1] as Document)["body"] }
    return ChangeStreamEvent(
      ChangeStreamDocument(
        OperationType.INSERT,
        resumeToken(resumeToken),
        null,
        null,
        Document("body", taskDocument(taskId, deleted, deleteTime)),
        null,
        null,
        null,
        null,
        null
      ), TaskDocument::class.java, converter
    )
  }

  private fun taskDocument(id: String, deleted: Boolean = false, deleteTime: Instant?) =
    TaskDocument(
      id,
      sourceReference = ProcessReferenceDocument("", "", "", "", "", ""),
      taskDefinitionKey = "",
      deleted = deleted,
      deleteTime = deleteTime
    )

  private fun task(id: String, deleted: Boolean = false) =
    Task(
      id,
      sourceReference = ProcessReference("", "", "", "", "", ""),
      taskDefinitionKey = "",
      deleted = deleted
    )

  private fun resumeToken(counter: Int) = BsonDocument("token", BsonString("token$counter"))
}

internal val Int.seconds get() = Duration.ofSeconds(this.toLong())
