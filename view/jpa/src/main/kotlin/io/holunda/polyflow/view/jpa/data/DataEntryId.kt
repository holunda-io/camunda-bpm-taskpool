package io.holunda.polyflow.view.jpa.data

import java.io.Serializable
import java.util.*
import javax.persistence.Column
import javax.persistence.Embeddable

/**
 * JPA composite id used for data entries.
 */
@Embeddable
class DataEntryId(
  @Column(name = "ENTRY_ID", nullable = false)
  var entryId: String,
  @Column(name = "ENTRY_TYPE", nullable = false)
  var entryType: String
) : Serializable {

  companion object {
    /**
     * Factgory method to construct the Data Entry Id from a string.
     */
    operator fun invoke(identity: String): DataEntryId = identity.split(":").let {
      require(it.size >= 2) { "Illegal identity format, expecting <entryType>:<entryId>, received '$identity'" }
      DataEntryId(entryType = it[0], entryId = it.subList(1, it.size).joinToString(":"))
    }
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is DataEntryId) return false
    return Objects.equals(this.entryId, other.entryId) &&
      Objects.equals(this.entryType, other.entryType)
  }

  override fun hashCode(): Int {
    return Objects.hash(this.entryId, this.entryType)
  }

  override fun toString(): String = "$entryType:$entryId"
}
