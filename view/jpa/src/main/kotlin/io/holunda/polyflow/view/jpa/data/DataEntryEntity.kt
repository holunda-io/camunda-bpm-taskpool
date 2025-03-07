package io.holunda.polyflow.view.jpa.data


import io.holunda.polyflow.view.jpa.payload.PayloadAttribute
import jakarta.persistence.*
import org.hibernate.annotations.Fetch
import org.hibernate.annotations.FetchMode
import org.hibernate.annotations.Immutable
import java.time.Instant

/**
 * Entity to store data entries.
 */
@Entity
@Table(name = "PLF_DATA_ENTRY")
class DataEntryEntity(
  @EmbeddedId
  var dataEntryId: DataEntryId,
  @Column(name = "TYPE", length = 255, nullable = false)
  var type: String,
  @Column(name = "NAME", length = 255, nullable = false)
  var name: String,
  @Column(name = "APPLICATION_NAME", length = 64, nullable = false)
  var applicationName: String,
  @Column(name = "FORM_KEY", length = 64, nullable = true)
  var formKey: String? = null,
  @Column(name = "REVISION")
  var revision: Long = 0L,
  @Embedded
  var state: DataEntryStateEmbeddable,
  @Column(name = "DESCRIPTION", nullable = true, length = 2048)
  var description: String? = null,

  @Column(name = "DATE_CREATED", nullable = false)
  var createdDate: Instant = Instant.now(),
  @Column(name = "DATE_LAST_MODIFIED", nullable = false)
  var lastModifiedDate: Instant = Instant.now(),
  @Column(name = "DATE_DELETED", nullable = true)
  var deletedDate: Instant? = null,

  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(
    name = "PLF_DATA_ENTRY_AUTHORIZATIONS",
    joinColumns = [
      JoinColumn(name = "ENTRY_TYPE", referencedColumnName = "ENTRY_TYPE"),
      JoinColumn(name = "ENTRY_ID", referencedColumnName = "ENTRY_ID"),
    ]
  )
  @Column(name = "AUTHORIZED_PRINCIPAL", nullable = false)
  var authorizedPrincipals: MutableSet<String> = mutableSetOf(),

  @Column(name = "VERSION_TIMESTAMP")
  var versionTimestamp: Long = 0L,

  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(
    name = "PLF_DATA_ENTRY_PAYLOAD_ATTRIBUTES",
    joinColumns = [
      JoinColumn(name = "ENTRY_TYPE", referencedColumnName = "ENTRY_TYPE"),
      JoinColumn(name = "ENTRY_ID", referencedColumnName = "ENTRY_ID"),
    ]
  )
  var payloadAttributes: MutableSet<PayloadAttribute> = mutableSetOf(),


  @OneToMany(mappedBy = "dataEntry", orphanRemoval = true, cascade = [CascadeType.ALL], fetch = FetchType.EAGER)
  @Fetch(FetchMode.SELECT)
  var protocol: MutableList<ProtocolElement> = mutableListOf(),
  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(
    name = "PLF_DATA_ENTRY_CORRELATIONS",
    joinColumns = [
      JoinColumn(name = "OWNING_ENTRY_TYPE", referencedColumnName = "ENTRY_TYPE"),
      JoinColumn(name= "OWNING_ENTRY_ID", referencedColumnName = "ENTRY_ID"),
    ]
  )
  var correlations: MutableSet<DataEntryId> = mutableSetOf(),
  @Immutable
  @OneToMany(fetch = FetchType.LAZY)
  @JoinColumns(
    JoinColumn(name = "ENTRY_TYPE", referencedColumnName = "ENTRY_TYPE", insertable = false, updatable = false),
    JoinColumn(name = "ENTRY_ID", referencedColumnName = "ENTRY_ID", insertable = false, updatable = false)
  )
  var payloadAndCorrelatedPayloadAttributes: MutableSet<DataEntryPayloadAttributeEntity> = mutableSetOf(),

  @Column(name = "PAYLOAD")
  @Lob
  var payload: String? = null,
) {
  override fun toString(): String {
    return "DataEntry[entryType = ${dataEntryId.entryType}, entryId = ${dataEntryId.entryId}, name = $name]"
  }
}

