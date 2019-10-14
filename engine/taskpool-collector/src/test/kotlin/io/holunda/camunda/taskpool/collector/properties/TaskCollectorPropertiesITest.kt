package io.holunda.camunda.taskpool.collector.properties

import io.holunda.camunda.taskpool.TaskCollectorProperties
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
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
class TaskCollectorPropertiesITest {

  @Autowired
  lateinit var props: TaskCollectorProperties

  @Test
  fun test_properties() {
    assertThat(props.enricher.applicationName).isEqualTo("Foo")
  }
}

