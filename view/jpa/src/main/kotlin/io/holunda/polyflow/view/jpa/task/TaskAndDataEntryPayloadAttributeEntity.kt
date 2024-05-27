package io.holunda.polyflow.view.jpa.task

import jakarta.persistence.EmbeddedId
import jakarta.persistence.Entity
import jakarta.persistence.Table

@Entity
@Table(name = "PLF_VIEW_TASK_AND_DATA_ENTRY_PAYLOAD")
class TaskAndDataEntryPayloadAttributeEntity(
  @EmbeddedId var id: TaskAndDataEntryPayloadAttributeEntityId
)
