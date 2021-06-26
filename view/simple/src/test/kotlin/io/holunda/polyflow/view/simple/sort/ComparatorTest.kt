package io.holunda.polyflow.view.simple.sort

import io.holunda.camunda.taskpool.api.task.ProcessReference
import io.holunda.polyflow.view.Task
import io.holunda.polyflow.view.TaskWithDataEntries
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import java.time.Instant


class ComparatorTest {

  private val now = Instant.now()
  private val ref = ProcessReference("1", "2", "3", "4", "My Process", "myExample")
  private val task0 = TaskWithDataEntries(Task("id", ref, "key", name = "aaa", priority = 17, createTime = now), listOf())
  private val task1 = TaskWithDataEntries(Task("id", ref, "key", name = "myName", priority = 31, createTime = now.plusSeconds(10)), listOf())
  private val task2 = TaskWithDataEntries(Task("id", ref, "key", name = "zzz", priority = 37, createTime = now.plusSeconds(20)), listOf())

  private val nameComparator = taskComparator("+task.name")!!
  private val priorityComparator = taskComparator("+task.priority")!!
  private val createTimeComparator = taskComparator("+task.createTime")!!
  private val descriptionComparator = taskComparator("+task.description")!!
  private val dueDateComparator = taskComparator("+task.dueDate")!!

  @Before
  fun precondition() {
    assertThat(nameComparator).isNotNull
    assertThat(priorityComparator).isNotNull
    assertThat(createTimeComparator).isNotNull
    assertThat(dueDateComparator).isNotNull
    assertThat(descriptionComparator).isNotNull
  }

  @Test
  fun `test greater less with strings`() {
    assertThat(nameComparator.compare(task0, task1)).isLessThan(0)
    assertThat(nameComparator.compare(task2, task1)).isGreaterThan(0)
  }

  @Test
  fun `test greater less with ints`() {
    assertThat(priorityComparator.compare(task0, task1)).isLessThan(0)
    assertThat(priorityComparator.compare(task2, task1)).isGreaterThan(0)
  }

  @Test
  fun `test greater less with dates`() {
    assertThat(createTimeComparator.compare(task0, task1)).isLessThan(0)
    assertThat(createTimeComparator.compare(task2, task1)).isGreaterThan(0)
  }


  @Test
  fun `test equality with strings`() {
    assertThat(nameComparator.compare(task1, task1.copy())).isEqualTo(0)
    assertThat(descriptionComparator.compare(task1, task1.copy())).isEqualTo(0)
  }

  @Test
  fun `test equality with ints`() {
    assertThat(priorityComparator.compare(task1, task1.copy())).isEqualTo(0)
  }

  @Test
  fun `test equality with dates`() {
    assertThat(createTimeComparator.compare(task1, task1.copy())).isEqualTo(0)
    assertThat(dueDateComparator.compare(task1, task1.copy())).isEqualTo(0)
  }
}
