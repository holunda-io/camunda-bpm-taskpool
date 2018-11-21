package io.holunda.camunda.taskpool.view.simple.service

import io.holunda.camunda.taskpool.api.task.ProcessReference
import io.holunda.camunda.taskpool.view.Task
import io.holunda.camunda.taskpool.view.TaskWithDataEntries
import io.holunda.camunda.taskpool.view.auth.User
import io.holunda.camunda.taskpool.view.query.TasksWithDataEntriesForUserQuery
import org.assertj.core.api.Assertions.assertThat
import org.axonframework.config.EventProcessingConfiguration
import org.axonframework.queryhandling.QueryUpdateEmitter
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock

class TaskPoolServiceTest {

  private lateinit var testee: TaskPoolService

  private val procRef = ProcessReference("instance1", "exec1", "def1", "def-key", "proce1", "app")
  private val bigList = (0..111).map { task(it) }
  private val smallList = (0..13).map { task(it) }

  private fun task(i: Int) = TaskWithDataEntries(Task("id$i", procRef, "task-key-$i", businessKey = "BUS-$i"))
  private fun query(page: Int, size: Int) = TasksWithDataEntriesForUserQuery(User("kermit", setOf()), page, size)

  @Before
  fun init() {
    testee = TaskPoolService(mock(QueryUpdateEmitter::class.java), mock(EventProcessingConfiguration::class.java))
  }


  @Test
  fun `slice contains complete list if input list is smaller than desired page size`() {
    val size = 83
    assertThat(testee.slice(smallList, query(0, size)).tasksWithDataEntries.size).isEqualTo(smallList.size)
  }

  @Test
  fun `slice for page 0 contains the desired count of elements`() {
    val size = 83
    assertThat(testee.slice(bigList, query(0, size)).tasksWithDataEntries.size).isEqualTo(size)
  }

  @Test
  fun `slice for page 1 contains elements from after page 0 to end of list`() {
    val size = 83
    assertThat(testee.slice(bigList, query(1, size)).tasksWithDataEntries.size).isEqualTo(bigList.size - size)
  }

  @Test
  fun `paging returns each element exactly once`() {
    val size = 83
    assertThat(testee.slice(bigList, query(0, size)).tasksWithDataEntries + testee.slice(bigList, query(1, size)).tasksWithDataEntries).isEqualTo(bigList)
  }

}
