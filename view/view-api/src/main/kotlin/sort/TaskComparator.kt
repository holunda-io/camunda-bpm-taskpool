package io.holunda.polyflow.view.sort

import io.holunda.polyflow.view.TaskWithDataEntries
import io.holunda.polyflow.view.filter.extractValue
import java.lang.reflect.Field
import javax.xml.datatype.DatatypeConstants.LESSER

/**
 * Comparator for Tasks with Data Entries
 */
data class TaskComparator(
  private val fieldSort: Pair<Field, SortDirection>
) : Comparator<TaskWithDataEntries> {
  override fun compare(o1: TaskWithDataEntries, o2: TaskWithDataEntries): Int {
    return try {

      val v1 = extractValue(o1.task, this.fieldSort.first)
      val v2 = extractValue(o2.task, this.fieldSort.first)

      when (findCompareMode(v1, v2)) {
        CompareMode.DEFAULT -> compareActual(fieldSort, v1, v2)
        CompareMode.LESS_THAN -> -1 * this.fieldSort.second.modifier
        CompareMode.GREATER_THAN -> 1 * this.fieldSort.second.modifier
        CompareMode.EQUAL -> 0 * this.fieldSort.second.modifier
      }
    } catch (e: Exception) {
      LESSER
    }
  }
}

