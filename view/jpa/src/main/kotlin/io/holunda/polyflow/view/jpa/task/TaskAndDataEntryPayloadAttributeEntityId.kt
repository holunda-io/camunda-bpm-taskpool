package io.holunda.polyflow.view.jpa.task

import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import java.io.Serializable

@Embeddable
class TaskAndDataEntryPayloadAttributeEntityId(
  @Column(name = "TASK_ID", length = 64, nullable = false)
  var taskId: String,
  @Column(name = "PATH", nullable = false)
  var path: String,
  @Column(name = "VALUE", nullable = false)
  var value: String
) : Serializable {

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as TaskAndDataEntryPayloadAttributeEntityId

    if (taskId != other.taskId) return false
    if (path != other.path) return false
    if (value != other.value) return false

    return true
  }

  override fun hashCode(): Int {
    var result = taskId.hashCode()
    result = 31 * result + path.hashCode()
    result = 31 * result + value.hashCode()
    return result
  }

  override fun toString(): String {
    return "TaskAndDataEntryPayloadAttributeEntityId(taskId='$taskId', path='$path', value='$value')"
  }

}
