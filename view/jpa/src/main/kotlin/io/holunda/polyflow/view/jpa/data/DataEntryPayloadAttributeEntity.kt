package io.holunda.polyflow.view.jpa.data

import jakarta.persistence.EmbeddedId
import jakarta.persistence.Entity
import jakarta.persistence.Table

@Entity
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
}

