package io.holunda.polyflow.view.jpa.data

/**
 * DTO representing authorized user or group.
 */
data class AuthorizationPrincipal(
  val name: String,
  val type: AuthorizationPrincipalType
) {
  companion object {
    /**
     * Factory method to construct principal out of string.
     */
    operator fun invoke(auth: String) = auth.split(":").let {
      require(it.size >= 2) { "Illegal auth format, expecting <type>:<name>, received '$auth'" }
      AuthorizationPrincipal(type = AuthorizationPrincipalType.valueOf(it[0]), name = it.subList(1, it.size).joinToString(":"))
    }

    /**
     * Constructor for the group principal.
     */
    fun group(name: String): AuthorizationPrincipal = AuthorizationPrincipal(name = name, type = AuthorizationPrincipalType.GROUP)
    /**
     * Constructor for the user principal.
     */
    fun user(name: String): AuthorizationPrincipal = AuthorizationPrincipal(name = name, type = AuthorizationPrincipalType.USER)
  }

  override fun toString(): String = "$type:$name"
}

