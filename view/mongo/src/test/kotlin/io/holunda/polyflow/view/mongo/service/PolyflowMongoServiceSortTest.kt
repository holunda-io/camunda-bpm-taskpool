package io.holunda.polyflow.view.mongo.service

import io.holunda.polyflow.view.mongo.sort
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.data.domain.Sort

class PolyflowMongoServiceSortTest {

  @Test
  fun `should be unsorted for null, empty or wrong sort`() {
    assertThat(sort(listOf())).isEqualTo(Sort.unsorted())
    assertThat(sort(null)).isEqualTo(Sort.unsorted())
    assertThat(sort("")).isEqualTo(Sort.unsorted())
    assertThat(sort("foo")).isEqualTo(Sort.unsorted())
  }

  @Test
  fun `should extract direction and field name`() {
    assertThat(sort("+foo")).isEqualTo(Sort.by(Sort.Direction.ASC, "foo"))
    assertThat(sort("-bar")).isEqualTo(Sort.by(Sort.Direction.DESC, "bar"))
    assertThat(sort("-b")).isEqualTo(Sort.by(Sort.Direction.DESC, "b"))
  }

  @Test
  fun `should combine sort`() {
    assertThat(sort(listOf("+foo", "-bar"))).isEqualTo(Sort.by(Sort.Direction.ASC, "foo").and(Sort.by(Sort.Direction.DESC, "bar")))
    assertThat(sort(listOf("+foo", ""))).isEqualTo(Sort.by(Sort.Direction.ASC, "foo"))
    assertThat(sort(listOf("", "foo"))).isEqualTo(Sort.unsorted())
  }


}
