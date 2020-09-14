package io.holunda.camunda.taskpool.view.simple.sort

import io.holunda.camunda.taskpool.view.DataEntry
import io.holunda.camunda.taskpool.view.simple.filter.extractValue
import java.lang.reflect.Field
import java.util.Comparator
import javax.xml.datatype.DatatypeConstants

/**
 * Comparator of data entries to be used in a sort.
 */
data class DataEntryComparator(
  private val fieldSort: Pair<Field, SortDirection>
) : Comparator<DataEntry> {

  override fun compare(o1: DataEntry, o2: DataEntry): Int {
    return try {

      val v1 = extractValue(o1, this.fieldSort.first)
      val v2 = extractValue(o2, this.fieldSort.first)

      when (findCompareMode(v1, v2)) {
        CompareMode.DEFAULT -> compareActual(fieldSort, v1, v2)
        CompareMode.LESS_THAN -> -1 * this.fieldSort.second.modifier
        CompareMode.GREATER_THAN -> 1 * this.fieldSort.second.modifier
        CompareMode.EQUAL -> 0 * this.fieldSort.second.modifier
      }
    } catch (e: Exception) {
      DatatypeConstants.LESSER
    }
  }
}
