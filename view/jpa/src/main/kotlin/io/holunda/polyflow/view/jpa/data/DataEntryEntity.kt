package io.holunda.polyflow.view.jpa.data


import java.time.Instant
import javax.persistence.*

/**
 * Entity to store data entries.
 */
@Entity(name = "DATA_ENTRY")
class DataEntryEntity(
  @EmbeddedId
  var dataEntryId: DataEntryId,
  @Column(name = "TYPE", nullable = false)
  var type: String,
  @Column(name = "NAME", nullable = false)
  var name: String,
  @Column(name = "APPLICATION_NAME", nullable = false)
  var applicationName: String,
  @Column(name = "FORM_KEY")
  var formKey: String? = null,
  @Column(name = "REVISION")
  var revision: Long = 0L,
  @Embedded
  var state: DataEntryStateEmbeddable,

  @Column(name = "DESCRIPTION")
  var description: String? = null,

  @Column(name = "DATE_CREATED", nullable = false)
  var createdDate: Instant = Instant.now(),

  @Column(name = "DATE_LAST_MODIFIED", nullable = false)
  var lastModifiedDate: Instant = Instant.now(),

  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(
    name = "DATA_ENTRY_AUTHORIZATIONS",
    joinColumns = [
      JoinColumn(name = "ENTRY_TYPE", referencedColumnName = "ENTRY_TYPE"),
      JoinColumn(name = "ENTRY_ID", referencedColumnName = "ENTRY_ID"),
    ]
  )
  @Column(name = "AUTHORIZED_PRINCIPAL", nullable = false)
  var authorizedPrincipals: Set<String> = setOf(),

  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(
    name = "DATA_PAYLOAD_ATTRIBUTES",
    joinColumns = [
      JoinColumn(name = "ENTRY_TYPE", referencedColumnName = "ENTRY_TYPE"),
      JoinColumn(name = "ENTRY_ID", referencedColumnName = "ENTRY_ID"),
    ]
  )
  @Column(name = "PAYLOAD_ATTRIBUTE", nullable = false)
  var payloadAttributes: Set<String> = setOf(),

  @OneToMany(mappedBy = "dataEntry", orphanRemoval = true, cascade = [CascadeType.ALL], targetEntity = ProtocolElement::class, fetch = FetchType.EAGER)
  var protocol: List<ProtocolElement> = mutableListOf(),

  @Lob
  var payload: String? = null,
) {
  override fun toString(): String {
    return "DataEntry[entryType = ${dataEntryId.entryType}, entryId = ${dataEntryId.entryId}, name = $name]"
  }
}

