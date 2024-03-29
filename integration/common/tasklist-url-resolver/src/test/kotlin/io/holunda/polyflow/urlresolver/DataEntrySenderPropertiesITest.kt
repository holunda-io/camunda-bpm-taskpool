package io.holunda.polyflow.urlresolver

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.PropertySource
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension::class)
@SpringBootTest(classes = [PropertiesTestApplication::class], webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ActiveProfiles("properties-itest")
@PropertySource
class DataEntrySenderPropertiesITest {

  @Autowired
  lateinit var props: TasklistUrlProperties

  @Test
  fun `should use defaults without properties in yaml`() {
    assertThat(props.tasklistUrl).isNull()
  }
}

