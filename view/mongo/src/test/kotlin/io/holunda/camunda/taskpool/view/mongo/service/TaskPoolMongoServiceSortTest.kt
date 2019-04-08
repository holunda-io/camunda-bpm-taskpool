package io.holunda.camunda.taskpool.view.mongo.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.springframework.data.domain.Sort

class TaskPoolMongoServiceSortTest {

  @Test
  fun `should be unsorted for null, empty or wrong sort`() {
    assertThat(sort(null)).isEqualTo(Sort.unsorted())
    assertThat(sort("")).isEqualTo(Sort.unsorted())
    assertThat(sort("foo")).isEqualTo(Sort.unsorted())
  }

  @Test
  fun `should extract direction and field name`() {
    assertThat(sort("+foo")).isEqualTo(Sort(Sort.Direction.ASC, "foo"))
    assertThat(sort("-bar")).isEqualTo(Sort(Sort.Direction.DESC, "bar"))
    assertThat(sort("-b")).isEqualTo(Sort(Sort.Direction.DESC, "b"))
  }

}
