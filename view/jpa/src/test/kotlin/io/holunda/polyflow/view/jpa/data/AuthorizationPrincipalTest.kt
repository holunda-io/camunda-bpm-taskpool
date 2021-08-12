package io.holunda.polyflow.view.jpa.data

import io.holunda.polyflow.view.jpa.data.AuthorizationPrincipal.Companion.group
import io.holunda.polyflow.view.jpa.data.AuthorizationPrincipal.Companion.user
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test

internal class AuthorizationPrincipalTest {

  @Test
  fun `should construct authorization principal id for group`() {
    val id = AuthorizationPrincipal("GROUP:groupName")
    assertThat(id).isEqualTo(group(name = "groupName"))
  }

  @Test
  fun `should construct authorization principal id for group created from a client role`() {
    val id = AuthorizationPrincipal("GROUP:client-id:groupName")
    assertThat(id).isEqualTo(group(name = "client-id:groupName"))
  }


  @Test
  fun `should construct authorization principal id for user`() {
    val id = AuthorizationPrincipal("USER:userName")
    assertThat(id).isEqualTo(user(name = "userName"))
  }

  @Test
  fun `should not construct authorization principal id`() {
    assertThatThrownBy { AuthorizationPrincipal("bad string") }.hasMessage("Illegal auth format, expecting <type>:<name>, received 'bad string'")
  }


}
