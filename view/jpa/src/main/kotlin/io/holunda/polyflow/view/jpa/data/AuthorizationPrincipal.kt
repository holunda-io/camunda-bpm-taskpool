package io.holunda.polyflow.view.jpa.data

/**
 * DTO representing authorized user or group.
 */
data class AuthorizationPrincipal(
  val name: String,
  val type: AuthorizationPrincipalType
) {
  companion object {
    operator fun invoke(auth: String) = auth.split(":").let {
      require(it.size == 2) { "Illegal auth format, expecting <type>:<name>" }
      AuthorizationPrincipal(type = AuthorizationPrincipalType.valueOf(it[0]), name = it[1])
    }

    fun group(name: String): AuthorizationPrincipal = AuthorizationPrincipal(name = name, type = AuthorizationPrincipalType.GROUP)
    fun user(name: String): AuthorizationPrincipal = AuthorizationPrincipal(name = name, type = AuthorizationPrincipalType.USER)
  }

  override fun toString(): String = "$type:$name"
}

