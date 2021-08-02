package io.holunda.polyflow.view.jpa.data


import io.holunda.polyflow.view.jpa.auth.AuthorizationPrincipal
import java.io.Serializable
import java.time.Instant
import java.util.*
import javax.persistence.*

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

  @ManyToMany(fetch = FetchType.EAGER, targetEntity = AuthorizationPrincipal::class)
  @JoinTable(
    name = "DATA_ENTRY_AUTHORIZATIONS",
    inverseJoinColumns = [
      JoinColumn(name = "AUTH_NAME", referencedColumnName = "AUTH_NAME"),
      JoinColumn(name = "AUTH_TYPE", referencedColumnName = "AUTH_TYPE"),
    ],
    joinColumns = [
      JoinColumn(name = "ENTRY_TYPE", referencedColumnName = "ENTRY_TYPE"),
      JoinColumn(name = "ENTRY_ID", referencedColumnName = "ENTRY_ID"),
    ],

    )
  var authorizedPrincipals: Set<AuthorizationPrincipal> = setOf(),

  @OneToMany(mappedBy = "dataEntry", orphanRemoval = true, cascade = [CascadeType.ALL], targetEntity = ProtocolElement::class)
  var protocol: List<ProtocolElement> = mutableListOf(),

  @Lob
  var payload: String? = null,
) {
  override fun toString(): String {
    return "DataEntry[entryType = ${dataEntryId.entryType}, entryId = ${dataEntryId.entryId}, name = $name]"
  }
}

@Embeddable
class DataEntryId(
  @Column(name = "ENTRY_ID", nullable = false)
  var entryId: String,
  @Column(name = "ENTRY_TYPE", nullable = false)
  var entryType: String
) : Serializable {

  companion object {
    operator fun invoke(identity: String): DataEntryId = identity.split(":").let {
      require(it.size == 2) { "Illegal identity format, expecting <entryType>:<entryId>" }
      DataEntryId(entryType = it[0], entryId = it[1])
    }
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is DataEntryId) return false
    return Objects.equals(this.entryId, other.entryId) &&
      Objects.equals(this.entryType, other.entryType)
  }

  override fun hashCode(): Int {
    return Objects.hash(this.entryId, this.entryType)
  }

  override fun toString(): String = "$entryType:$entryId"
}
