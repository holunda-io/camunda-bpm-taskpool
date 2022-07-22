package io.holunda.polyflow.datapool

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
@ActiveProfiles("properties-itest")
@PropertySource
class DataEntrySenderPropertiesITest {

  @Autowired
  lateinit var props: DataEntrySenderProperties

  @Test
  fun `should use defaults without properties in yaml`() {
    assertThat(props.applicationName).isEqualTo("Foo")
    assertThat(props.enabled).isFalse
    assertThat(props.type).isEqualTo(DataEntrySenderType.simple)
  }
}

