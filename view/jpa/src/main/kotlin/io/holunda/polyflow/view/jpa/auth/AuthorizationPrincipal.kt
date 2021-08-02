package io.holunda.polyflow.view.jpa.auth

import java.io.Serializable
import java.util.*
import javax.persistence.*

/**
 * Authorization principal (user or group).
 */
@Entity(name = "AUTHORIZATION_PRINCIPAL")
class AuthorizationPrincipal(
  @EmbeddedId
  var id: AuthorizationPrincipalId
) {
  companion object {
    fun group(name: String) = AuthorizationPrincipal(AuthorizationPrincipalId.invoke("${AuthorizationPrincipalType.GROUP.name}:$name"))
    fun user(name: String) = AuthorizationPrincipal(AuthorizationPrincipalId.invoke("${AuthorizationPrincipalType.USER.name}:$name"))
  }
}

/**
 * Composite to use for authorization.
 */
@Embeddable
class AuthorizationPrincipalId(
  @Column(name = "AUTH_NAME", nullable = false)
  var name: String,
  @Column(name = "AUTH_TYPE", nullable = false)
  @Enumerated(EnumType.STRING)
  var type: AuthorizationPrincipalType
) : Serializable {
  companion object {
    operator fun invoke(auth: String) = auth.split(":").let {
      require(it.size == 2) { "Illegal auth format, expecting <type>:<name>" }
      AuthorizationPrincipalId(type = AuthorizationPrincipalType.valueOf(it[0]), name = it[1])
    }
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is AuthorizationPrincipalId) return false
    return Objects.equals(this.name, other.name) &&
      Objects.equals(this.type, other.type)
  }

  override fun hashCode(): Int {
    return Objects.hash(this.name, this.type)
  }

  override fun toString(): String = "$type:$name"

}

/**
 * Authorization type.
 */
enum class AuthorizationPrincipalType {
  GROUP,
  USER
}
