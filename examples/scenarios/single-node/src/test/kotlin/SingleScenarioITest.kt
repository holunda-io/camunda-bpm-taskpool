package io.holunda.polyflow.example.process.approval

import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner

@RunWith(SpringRunner::class)
@SpringBootTest
class SingleScenarioITest {

  @Autowired
  lateinit var userService: SimpleCurrentUserService

  @Before
  fun `init user`() {
    userService.currentUserStore.username = "kermit"
  }

  @Test
  fun `should start application`() {
    assertThat(userService.getCurrentUser()).isEqualTo("kermit")
  }
}
