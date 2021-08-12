package io.holunda.polyflow.view.jpa.data

import java.time.Instant
import java.util.*
import javax.persistence.*

@Entity(name = "PROTOCOL")
class ProtocolElement(
  @Id
  @Column(name = "ID")
  var id: String = UUID.randomUUID().toString(),

  @Column(name = "TIME", nullable = false)
  var time: Instant = Instant.now(),
  @Embedded
  var state: DataEntryStateEmbeddable,
  @Column(name = "USERNAME", nullable = true)
  var username: String? = null,
  @Column(name = "LOG_MESSAGE", nullable = true)
  var logMessage: String? = null,
  @Column(name = "LOG_DETAILS", nullable = true)
  var logDetails: String? = null,

  @ManyToOne
  @JoinColumns(
    JoinColumn(name = "ENTRY_TYPE", referencedColumnName = "ENTRY_TYPE", nullable = false),
    JoinColumn(name = "ENTRY_ID", referencedColumnName = "ENTRY_ID", nullable = false)
  )
  var dataEntry: DataEntryEntity
) {


  /**
   * Checks if the protocol element is the same as provided.
   */
  fun same(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as ProtocolElement

    if (time != other.time) return false
    if (state != other.state) return false
    if (username != other.username) return false
    if (logMessage != other.logMessage) return false
    if (logDetails != other.logDetails) return false
    if (dataEntry != other.dataEntry) return false

    return true
  }

}
