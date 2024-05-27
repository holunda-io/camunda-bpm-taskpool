package io.holunda.polyflow.view.jpa.task

import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import java.io.Serializable

@Embeddable
class TaskAndDataEntryPayloadAttributeEntityId(
  @Column(name = "TASK_ID", nullable = false) var taskId: String?,
  @Column(name = "PATH", nullable = false) var path: String?,
  @Column(name = "VALUE", nullable = false) var value: String?
) : Serializable
