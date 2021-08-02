package io.holunda.polyflow.view.jpa.auth

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test

internal class AuthorizationPrincipalIdTest {

  @Test
  fun `should construct authorization principal id for group`() {
    val id = AuthorizationPrincipalId("GROUP:groupName")
    assertThat(id).isEqualTo(AuthorizationPrincipalId(type = AuthorizationPrincipalType.GROUP, name = "groupName"))
  }

  @Test
  fun `should construct authorization principal id for user`() {
    val id = AuthorizationPrincipalId("USER:userName")
    assertThat(id).isEqualTo(AuthorizationPrincipalId(type = AuthorizationPrincipalType.USER, name = "userName"))
  }

  @Test
  fun `should not construct authorization principal id`() {
    assertThatThrownBy { AuthorizationPrincipalId("bad string") }.hasMessage("Illegal auth format, expecting <type>:<name>")
  }


}
