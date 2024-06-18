package io.holunda.polyflow.view.jpa.data

import jakarta.persistence.EmbeddedId
import jakarta.persistence.Entity
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable

/**
 * Entity that holds the combined payload attributes of the correlated DataEntries.
 */
@Entity
@Immutable
@Table(name = "PLF_VIEW_DATA_ENTRY_PAYLOAD")
class DataEntryPayloadAttributeEntity(
  @EmbeddedId
  var id: DataEntryPayloadAttributeEntityId,
) {
  constructor(entryType: String, entryId: String, path: String, value: String) : this(
    DataEntryPayloadAttributeEntityId(
      entryType = entryType,
      entryId = entryId,
      path = path,
      value = value
    )
  )

  override fun toString(): String = "DataEntryPayloadAttributeEntity(id=$id)"
}

