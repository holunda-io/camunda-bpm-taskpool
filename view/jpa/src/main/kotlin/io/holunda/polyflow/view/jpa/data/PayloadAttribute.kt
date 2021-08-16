package io.holunda.polyflow.view.jpa.data

import java.io.Serializable
import javax.persistence.Column
import javax.persistence.Embeddable

@Embeddable
class PayloadAttribute(
  @Column(name = "PATH", nullable = false)
  var path: String?,
  @Column(name = "VALUE", nullable = false)
  var value: String?
) : Serializable {

  companion object {
    operator fun invoke(entry: Map.Entry<String, Any>) = PayloadAttribute(path = entry.key, value = entry.value.toString())
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as PayloadAttribute

    if (path != other.path) return false
    if (value != other.value) return false

    return true
  }

  override fun hashCode(): Int {
    var result = path?.hashCode() ?: 0
    result = 31 * result + (value?.hashCode() ?: 0)
    return result
  }
}
