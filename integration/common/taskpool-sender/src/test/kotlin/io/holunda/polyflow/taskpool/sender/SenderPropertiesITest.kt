package io.holunda.polyflow.taskpool.sender

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.PropertySource
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner

@RunWith(SpringRunner::class)
@SpringBootTest(classes = [PropertiesTestApplication::class], webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ActiveProfiles("properties-itest") // intentionally pointing to a non-existing profile defaulting to empty property set.
@PropertySource
class SenderPropertiesITest {

  @Autowired
  lateinit var props: SenderProperties

  @Test
  fun `should use defaults without properties in yaml`() {
    assertThat(props.enabled).isTrue
    assertThat(props.task.enabled).isTrue
    assertThat(props.task.sendWithinTransaction).isFalse
    assertThat(props.task.type).isEqualTo(SenderType.tx)

    assertThat(props.processInstance.enabled).isTrue
    assertThat(props.processInstance.type).isEqualTo(SenderType.simple)

    assertThat(props.processDefinition.enabled).isFalse
    assertThat(props.processDefinition.type).isEqualTo(SenderType.simple)

    assertThat(props.processVariable.enabled).isTrue
    assertThat(props.processVariable.type).isEqualTo(SenderType.tx)
    assertThat(props.processVariable.sendWithinTransaction).isFalse
  }
}

