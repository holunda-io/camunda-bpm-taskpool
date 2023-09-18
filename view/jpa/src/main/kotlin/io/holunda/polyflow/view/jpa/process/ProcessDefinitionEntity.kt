package io.holunda.polyflow.view.jpa.process

import jakarta.persistence.*

/**
 * Entity to store process definitions.
 */
@Entity
@Table(name = "PLF_PROC_DEF")
class ProcessDefinitionEntity(
  @Id
  @Column(name = "PROC_DEF_ID", length = 255)
  var processDefinitionId: String,
  @Column(name = "PROC_DEF_KEY", length = 255, nullable = false)
  var processDefinitionKey: String,
  @Column(name = "PROC_DEF_VERSION", nullable = false)
  var processDefinitionVersion: Int,
  @Column(name = "APPLICATION_NAME", length = 64, nullable = false)
  var applicationName: String,
  @Column(name = "NAME", length = 255, nullable = false)
  var name: String,
  @Column(name = "VERSION_TAG")
  var versionTag: String? = null,
  @Column(name = "DESCRIPTION", length = 2048)
  var description: String? = null,
  @Column(name = "START_FORM_KEY")
  var startFormKey: String? = null,
  @Column(name = "STARTABLE_FROM_TASKLIST")
  var startableFromTasklist: Boolean = true,
  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(
    name = "PLF_PROC_DEF_AUTHORIZATIONS",
    joinColumns = [
      JoinColumn(name = "PROC_DEF_ID", referencedColumnName = "PROC_DEF_ID"),
    ]
  )
  @Column(name = "AUTHORIZED_STARTER_PRINCIPAL", nullable = false)
  var authorizedStarterPrincipals: MutableSet<String> = mutableSetOf()
)
