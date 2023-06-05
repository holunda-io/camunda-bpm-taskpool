package io.holunda.polyflow.view.filter

import io.holunda.camunda.taskpool.api.task.ProcessReference
import io.holunda.polyflow.view.DataEntry
import io.holunda.polyflow.view.Task
import io.holunda.polyflow.view.TaskWithDataEntries
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.camunda.bpm.engine.variable.Variables
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.temporal.ChronoUnit

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
    assertThat(isTaskAttribute("task.followUpDate")).isTrue
    assertThat(isTaskAttribute("task.dueDate")).isTrue
    assertThat(isTaskAttribute("task.processName")).isTrue
    assertThat(isTaskAttribute("task.textSearch")).isTrue
    assertThat(isTaskAttribute("task.businessKey")).isTrue

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
    assertThatThrownBy {
      toCriteria(listOf("$EQUALS$EQUALS"))
    }
  }

  @Test
  fun `should create task criteria for process name and text search`() {
    val criteria = toCriteria(listOf("task.processName${LIKE}foo", "task.textSearch${LIKE}bar"))
    assertThat(criteria).isNotNull
    assertThat(criteria.size).isEqualTo(2)
    assertThat(criteria).containsExactlyElementsOf(
      listOf(
        Criterion.TaskCriterion("processName", "foo", operator = LIKE),
        Criterion.TaskCriterion("textSearch", "bar", operator = LIKE),
      )
    )
  }


  @Test
  fun `should fail to create criteria 2`() {
    assertThatThrownBy {
      toCriteria(listOf("${EQUALS}some$EQUALS"))
    }
  }

  @Test
  fun `should ignore wrong format`() {
    assertThat(toCriteria(listOf("noSeparator"))).isEmpty()
  }

  @Test
  fun `should trim params`() {
    val criteria = toCriteria(listOf("task.name${EQUALS}some"))
    val predicates = createTaskPredicates(criteria)
    assertThat(predicates).isNotNull
    assertThat(predicates.taskAttributePredicate).isNotNull

    val criteriaWithSpaces = toCriteria(listOf("task.name $EQUALS some"))
    val predicatesTrimmed = createTaskPredicates(criteriaWithSpaces)
    assertThat(predicatesTrimmed).isNotNull
    assertThat(predicatesTrimmed.taskAttributePredicate).isNotNull
  }


  @Suppress("UNNECESSARY_NOT_NULL_ASSERTION")
  @Test
  fun `should create predicates`() {

    val criteria = toCriteria(filtersList)
    val predicates = createTaskPredicates(criteria)

    assertThat(predicates.taskAttributePredicate).isNotNull
    assertThat(predicates.taskPayloadPredicate).isNotNull

    assertThat(predicates.taskAttributePredicate!!.test(task1.task)).isFalse
    assertThat(predicates.taskAttributePredicate!!.test(task2.task)).isFalse
    assertThat(predicates.taskAttributePredicate!!.test(task3.task)).isFalse
    assertThat(predicates.taskPayloadPredicate!!.test(task3.dataEntries[0].payload)).isFalse
    assertThat(predicates.taskPayloadPredicate!!.test(task4.dataEntries[0].payload)).isFalse
    assertThat(predicates.taskPayloadPredicate!!.test(task5.dataEntries[0].payload)).isFalse
  }

  @Test
  fun `should filter string properties`() {

    assertThat(
      filter(
        listOf("task.name${EQUALS}myName", "task.priority${EQUALS}90"),
        listOf(task1, task2, task3, task4, task5, task6)
      )
    ).containsExactlyElementsOf(listOf(task1))

    assertThat(
      filter(
        listOf("task.assignee${EQUALS}gonzo"),
        listOf(task1, task2, task3, task4, task5, task6)
      )
    ).containsExactlyElementsOf(listOf(task3, task4, task5, task6))


    assertThat(
      filter(
        listOf("task.assignee${EQUALS}gonzo", "task.priority${EQUALS}78"),
        listOf(task1, task2, task3, task4, task5, task6)
      )
    ).containsExactlyElementsOf(listOf(task4))

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
  fun `should filter by less instant property`() {
    val now = Instant.now()
    val dueDate = now.minus(1, ChronoUnit.DAYS)

    val task = task1.copy(task = task1.task.copy(dueDate = dueDate))


    val dueDateFilter = listOf("task.dueDate<$now")
    val filtered = filter(dueDateFilter, listOf(task))
    assertThat(filtered).containsExactlyElementsOf(listOf(task))
  }

  @Test
  fun `should filter by greater number property`() {

    val numberFilter = listOf("task.priority>80")
    val filtered = filter(numberFilter, listOf(task1, task2, task3, task4, task5, task6))
    assertThat(filtered).containsExactlyElementsOf(listOf(task1, task2))
  }

  @Test
  fun `should filter by greater instant property`() {
    val now = Instant.now()
    val dueDate = now.plus(1, ChronoUnit.DAYS)

    val task = task1.copy(task = task1.task.copy(dueDate = dueDate))


    val dueDateFilter = listOf("task.dueDate>$now")
    val filtered = filter(dueDateFilter, listOf(task))
    assertThat(filtered).containsExactlyElementsOf(listOf(task))
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

