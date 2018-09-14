package io.holunda.camunda.taskpool.api.task

import io.holunda.camunda.taskpool.api.business.WithCorrelations
import java.util.*

interface TaskCommand : TaskIdentity, WithPayload, WithCorrelations, CamundaTaskEvent {
  val name: String?
  val description: String?
  val formKey: String?
  val createTime: Date?
  val owner: String?
  val assignee: String?
  val candidateUsers: List<String>
  val candidateGroups: List<String>
  val dueDate: Date?
  val priority: Int?
}
