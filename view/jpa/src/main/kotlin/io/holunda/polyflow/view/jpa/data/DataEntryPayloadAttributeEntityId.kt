package io.holunda.polyflow.view.jpa.data

import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import org.hibernate.annotations.Immutable
import java.io.Serializable

/**
 * Id class that holds the combined payload attributes of the correlated DataEntries.
 */
@Embeddable
@Immutable
class DataEntryPayloadAttributeEntityId(
  @Column(name = "ENTRY_TYPE", length = 64, nullable = false, updatable = false, insertable = false)
  var entryType: String,
  @Column(name = "ENTRY_ID", length = 64, nullable = false, updatable = false, insertable = false)
  var entryId: String,
  @Column(name = "PATH", nullable = false, updatable = false, insertable = false)
  var path: String,
  @Column(name = "VALUE", nullable = false, updatable = false, insertable = false)
  var value: String
) : Serializable {

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as DataEntryPayloadAttributeEntityId

    if (entryType != other.entryType) return false
    if (entryId != other.entryId) return false
    if (path != other.path) return false
    if (value != other.value) return false

    return true
  }

  override fun hashCode(): Int {
    var result = entryType.hashCode()
    result = 31 * result + entryId.hashCode()
    result = 31 * result + path.hashCode()
    result = 31 * result + value.hashCode()
    return result
  }

  override fun toString(): String {
    return "DataEntryPayloadAttributeEntityId(entryType='$entryType', entryId='$entryId', path='$path', value='$value')"
  }
}
