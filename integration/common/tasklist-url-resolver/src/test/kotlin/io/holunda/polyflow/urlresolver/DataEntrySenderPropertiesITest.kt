package io.holunda.polyflow.urlresolver

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.PropertySource
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner

@RunWith(SpringRunner::class)
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

