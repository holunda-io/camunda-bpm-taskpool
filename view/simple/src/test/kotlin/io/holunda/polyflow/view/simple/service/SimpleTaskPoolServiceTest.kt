package io.holunda.polyflow.view.simple.service

import io.holunda.camunda.taskpool.api.business.DataEntryCreatedEvent
import io.holunda.camunda.taskpool.api.task.ProcessReference
import io.holunda.camunda.taskpool.api.task.TaskCreatedEngineEvent
import io.holunda.polyflow.view.query.task.TasksForApplicationQuery
import org.assertj.core.api.Assertions
import org.axonframework.queryhandling.QueryUpdateEmitter
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import reactor.core.publisher.Flux
import reactor.core.scheduler.Schedulers
import java.time.Duration

internal class SimpleTaskPoolServiceTest {
  private val testee = SimpleTaskPoolService(mock(QueryUpdateEmitter::class.java))

  // This test is here mainly to show performance issues with the implementation
  @Test
  fun `can handle lots of data`() {
    Flux.range(1, 10_000)
      .publishOn(Schedulers.parallel())
      .doOnNext{testee.on(DataEntryCreatedEvent("type", "id-$it", "", "", ""))}
      .sample(Duration.ofSeconds(1))
      .timed()
      .doOnNext{ println("Added ${it.get()} DataEntries in ${it.elapsedSinceSubscription()}")}
      .blockLast()
    Flux.range(1, 10_000)
      .publishOn(Schedulers.parallel())
      .doOnNext{testee.on(TaskCreatedEngineEvent("task-$it", ProcessReference("", "", "", "", "", ""), ""))}
      .sample(Duration.ofSeconds(1))
      .timed()
      .doOnNext{ println("Added ${it.get()} Tasks in ${it.elapsedSinceSubscription()}")}
      .blockLast()

    Assertions.assertThat(testee.query(TasksForApplicationQuery("")).totalElementCount).isEqualTo(10_000)
  }
}
