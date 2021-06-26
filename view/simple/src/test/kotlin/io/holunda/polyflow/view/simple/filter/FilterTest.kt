package io.holunda.polyflow.view.simple.filter

import io.holunda.camunda.taskpool.api.task.ProcessReference
import io.holunda.polyflow.view.DataEntry
import io.holunda.polyflow.view.Task
import io.holunda.polyflow.view.TaskWithDataEntries
import org.assertj.core.api.Assertions.assertThat
import org.camunda.bpm.engine.variable.Variables
import org.junit.Test
import org.junit.jupiter.api.assertThrows

class FilterTest {

  private val filtersList = listOf("task.name${EQUALS}myName", "task.assignee${EQUALS}kermit", "dataAttr1${EQUALS}value", "dataAttr2${EQUALS}another")

  private val ref = ProcessReference("1", "2", "3", "4", "My Process", "myExample")

  // no match: task.assignee, dataAttr1, dataAttr2
  // match: task.name
  private val task1 = TaskWithDataEntries(Task("id", ref, "key", name = "myName", priority = 90), listOf())

  // no match: task.name, dataAttr1, dataAttr2
  // match: task.assignee
  private val task2 = TaskWithDataEntries(Task("id", ref, "key", assignee = "kermit", priority = 91), listOf())

  // no match: task.name, task.assignee, dataAttr2
  // match: dataEntries[0].payload -> dataAttr1
  private val task3 = TaskWithDataEntries(
    Task("id", ref, "key", name = "foo", assignee = "gonzo", priority = 80), listOf(
      DataEntry(
        entryType = "type",
        entryId = "4711",
        type = "type",
        applicationName = "app1",
        name = "name",
        payload = Variables.createVariables().putValue("dataAttr1", "another")
      )
    )
  )

  // no match: task.name, task.assignee, dataAttr2
  // match: dataEntries[0].payload -> dataAttr1
  private val task4 = TaskWithDataEntries(
    Task("id", ref, "key", name = "foo", assignee = "gonzo", priority = 78), listOf(
      DataEntry(
        entryType = "type",
        entryId = "4711",
        type = "type",
        applicationName = "app1",
        name = "name",
        payload = Variables.createVariables().putValue("dataAttr1", "value")
      )
    )
  )

  // no match: task.name, task.assignee, dataAttr1
  // match: dataEntries[0].payload -> dataAttr2
  private val task5 = TaskWithDataEntries(
    Task("id", ref, "key", name = "foo", assignee = "gonzo", priority = 80), listOf(
      DataEntry(
        entryType = "type",
        entryId = "4711",
        type = "type",
        applicationName = "app1",
        name = "name",
        payload = Variables.createVariables().putValue("dataAttr2", "another").putValue("name", "myName")
      )
    )
  )

  // no match: task.name, task.assignee, dataAttr1
  // match: task.payload -> dataAttr2
  private val task6 = TaskWithDataEntries(
    Task(
      "id", ref, "key", name = "foo", assignee = "gonzo", priority = 1,
      payload = Variables.createVariables().putValue("dataAttr2", "another").putValue("name", "myName")
    ), listOf()
  )


  @Test
  fun `should classify properties`() {
    assertThat(isTaskAttribute("task.id")).isTrue
    assertThat(isTaskAttribute("task.name")).isTrue
    assertThat(isTaskAttribute("task.assignee")).isTrue

    assertThat(isTaskAttribute("task.")).isFalse
    assertThat(isTaskAttribute("assignee")).isFalse
    assertThat(isTaskAttribute("someOther")).isFalse
    assertThat(isTaskAttribute("described")).isFalse
  }

  @Test
  fun `should create criteria`() {

    val criteria = toCriteria(filtersList)

    assertThat(criteria).isNotNull
    assertThat(criteria.size).isEqualTo(4)
    assertThat(criteria).containsExactlyElementsOf(
      listOf(
        Criterion.TaskCriterion("name", "myName"), Criterion.TaskCriterion("assignee", "kermit"),
        Criterion.PayloadEntryCriterion("dataAttr1", "value"), Criterion.PayloadEntryCriterion("dataAttr2", "another")
      )
    )
  }

  @Test
  fun `should fail to create criteria`() {
    assertThrows<IllegalArgumentException> {
      toCriteria(listOf("$EQUALS$EQUALS"))
    }
  }

  @Test
  fun `should fail to create criteria 2`() {
    assertThrows<IllegalArgumentException> {
      toCriteria(listOf("${EQUALS}some$EQUALS"))
    }
  }

  @Test
  fun `should ignore wrong format`() {
    assertThat(toCriteria(listOf("noSeparator"))).isEmpty()
  }

  @Test
  fun `should create predicates`() {

    val criteria = toCriteria(filtersList)
    val predicates = createTaskPredicates(criteria)

    assertThat(predicates).isNotNull
    assertThat(predicates.taskPredicate).isNotNull
    assertThat(predicates.dataEntriesPredicate).isNotNull

    assertThat(predicates.taskPredicate!!.test(task1.task)).isTrue
    assertThat(predicates.taskPredicate!!.test(task2.task)).isTrue
    assertThat(predicates.taskPredicate!!.test(task3.task)).isFalse
    assertThat(predicates.dataEntriesPredicate!!.test(task3.dataEntries[0].payload)).isFalse
    assertThat(predicates.dataEntriesPredicate!!.test(task4.dataEntries[0].payload)).isTrue
    assertThat(predicates.dataEntriesPredicate!!.test(task5.dataEntries[0].payload)).isTrue
  }

  @Test
  fun `should filter string properties`() {
    val filtered = filter(filtersList, listOf(task1, task2, task3, task4, task5, task6))
    assertThat(filtered).containsExactlyElementsOf(listOf(task1, task2, task4, task5, task6))
  }


  @Test
  fun `should filter by equal number property`() {

    val numberFilter = listOf("task.priority=80")
    val filtered = filter(numberFilter, listOf(task1, task2, task3, task4, task5, task6))
    assertThat(filtered).containsExactlyElementsOf(listOf(task3, task5))
  }

  @Test
  fun `should filter by less number property`() {

    val numberFilter = listOf("task.priority<80")
    val filtered = filter(numberFilter, listOf(task1, task2, task3, task4, task5, task6))
    assertThat(filtered).containsExactlyElementsOf(listOf(task4, task6))
  }

  @Test
  fun `should filter by greater number property`() {

    val numberFilter = listOf("task.priority>80")
    val filtered = filter(numberFilter, listOf(task1, task2, task3, task4, task5, task6))
    assertThat(filtered).containsExactlyElementsOf(listOf(task1, task2))
  }

  @Test
  fun `should filter by less string property`() {

    val numberFilter = listOf("task.assignee<zo")
    val filtered = filter(numberFilter, listOf(task1, task2, task3, task4, task5, task6))
    assertThat(filtered).containsExactlyElementsOf(listOf(task3, task4, task5, task6))
  }

  @Test
  fun `should filter by greater string property`() {

    val numberFilter = listOf("task.assignee>ke")
    val filtered = filter(numberFilter, listOf(task1, task2, task3, task4, task5, task6))
    assertThat(filtered).containsExactlyElementsOf(listOf(task2))
  }

}

