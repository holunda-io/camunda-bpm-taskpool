package io.holunda.camunda.client

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension::class)
@EnableConfigurationProperties(CamundaEngineClientProperties::class)
@TestPropertySource("classpath:itestdefaults.properties")
class CamundaEngineClientPropertiesDefaultsITest {

  @Autowired
  lateinit var props: CamundaEngineClientProperties

  @Test
  fun test_properties() {
    assertThat(props.applicationName).isEqualTo("Foo")
  }
}

