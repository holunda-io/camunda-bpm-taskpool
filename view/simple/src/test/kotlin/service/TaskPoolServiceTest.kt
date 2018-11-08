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
  fun `retrieve all`() {
    val size = 83
    assertThat(testee.slice(smallList, query(0, size)).tasksWithDataEntries.size).isEqualTo(smallList.size)
  }

  @Test
  fun `slice from 0`() {
    val size = 83
    assertThat(testee.slice(bigList, query(0, size)).tasksWithDataEntries.size).isEqualTo(size + 1)
  }

  @Test
  fun `slice from 1`() {
    val size = 83
    assertThat(testee.slice(bigList, query(1, size)).tasksWithDataEntries.size).isEqualTo(bigList.size - size)
  }

}
