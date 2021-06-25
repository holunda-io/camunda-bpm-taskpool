package io.holunda.polyflow.view.mongo.service

import com.mongodb.MongoCommandException
import com.mongodb.ServerAddress
import com.mongodb.client.model.changestream.ChangeStreamDocument
import com.mongodb.client.model.changestream.OperationType
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import io.holunda.camunda.taskpool.api.task.ProcessReference
import io.holunda.camunda.taskpool.view.Task
import io.holunda.camunda.taskpool.view.mongo.repository.DataEntryRepository
import io.holunda.camunda.taskpool.view.mongo.repository.ProcessReferenceDocument
import io.holunda.camunda.taskpool.view.mongo.repository.TaskDocument
import io.holunda.camunda.taskpool.view.mongo.repository.TaskRepository
import org.bson.BsonDocument
import org.bson.BsonString
import org.bson.Document
import org.junit.Test
import org.springframework.data.mongodb.core.ChangeStreamEvent
import org.springframework.data.mongodb.core.convert.MongoConverter
import reactor.core.Disposable
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import reactor.test.publisher.TestPublisher
import java.time.Duration

internal class TaskChangeTrackerTest {
  private val taskRepository: TaskRepository = mock()
  private val dataEntryRepository: DataEntryRepository = mock()

  // Lazy initialization is needed for StepVerifier.withVirtualTime to work properly
  private val taskChangeTracker by lazy { TaskChangeTracker(taskRepository, dataEntryRepository) }

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
      .then { initialPublisher.next(changeStreamEvent(1, "123", deleted = true)) }
      .expectNext(task("123", deleted = true))
      .then { deleteResult.assertWasSubscribed() }
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
      .then { initialPublisher.next(changeStreamEvent(1, "123", deleted = true)) }
      .expectNext(task("123", deleted = true))
      .then { initialPublisher.next(changeStreamEvent(1, "456", deleted = true)) }
      .expectNext(task("456", deleted = true))
      .then { deleteResult.assertWasSubscribed() }
      .verifyTimeout(10.seconds)
  }

  private fun publisherForResumeToken(resumeToken: Int? = null): TestPublisher<ChangeStreamEvent<TaskDocument>> =
    publisher().returnForResumeToken(resumeToken)

  private fun publisher() = TestPublisher.create<ChangeStreamEvent<TaskDocument>>()

  private fun TestPublisher<ChangeStreamEvent<TaskDocument>>.returnForResumeToken(resumeToken: Int?) = apply {
    whenever(taskRepository.getTaskUpdates(resumeToken?.let { resumeToken(it) })).thenReturn(this.flux())
  }

  private fun changeStreamEvent(resumeToken: Int, taskId: String, deleted: Boolean = false): ChangeStreamEvent<TaskDocument> {
    val converter: MongoConverter = mock()
    whenever(converter.read(eq(TaskDocument::class.java), any())).thenAnswer { (it.arguments[1] as Document)["body"] }
    return ChangeStreamEvent(ChangeStreamDocument(OperationType.INSERT, resumeToken(resumeToken), null, null, Document("body", taskDocument(taskId, deleted)), null, null, null, null, null), TaskDocument::class.java, converter)
  }

  private fun taskDocument(id: String, deleted: Boolean = false) =
    TaskDocument(
      id,
      sourceReference = ProcessReferenceDocument("", "", "", "", "", ""),
      taskDefinitionKey = "",
      deleted = deleted
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
