package io.holunda.polyflow.view.jpa.process

import io.holunda.polyflow.view.ProcessInstanceState
import javax.persistence.*

/**
 * Entity to store process instance.
 */
@Entity
@Table(name = "PLF_PROC_INSTANCE")
class ProcessInstanceEntity(
  @Id
  @Column(name = "INSTANCE_ID", nullable = true)
  var processInstanceId: String,
  @Column(name = "BUSINESS_KEY", nullable = true)
  var businessKey: String?,
  @Column(name = "SUPER_INSTANCE_ID", nullable = true)
  var superInstanceId: String?,
  @Column(name = "START_ACTIVITY_ID", nullable = true)
  var startActivityId: String?,
  @Column(name = "END_ACTIVITY_ID", nullable = true)
  var endActivityId: String?,
  @Column(name = "DELETE_REASON", nullable = true)
  var deleteReason: String?,
  @Column(name = "START_USER_ID", nullable = true)
  var startUserId: String?,
  var sourceReference: ProcessSourceReferenceEmbeddable,
  @Column(name = "RUN_STATE", nullable = false)
  @Enumerated(EnumType.STRING)
  var state: ProcessInstanceState
)
