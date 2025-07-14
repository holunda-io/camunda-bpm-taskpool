package io.holunda.polyflow.view.sort

import io.holunda.polyflow.view.DataEntry
import io.holunda.polyflow.view.filter.extractValue
import java.lang.reflect.Field
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
