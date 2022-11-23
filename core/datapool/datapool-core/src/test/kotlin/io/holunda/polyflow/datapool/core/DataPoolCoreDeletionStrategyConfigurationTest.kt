package io.holunda.polyflow.datapool.core

import org.assertj.core.api.Assertions.assertThat
import org.axonframework.eventsourcing.Snapshotter
import org.axonframework.eventsourcing.eventstore.EventStore
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.mock
import org.springframework.boot.context.annotation.UserConfigurations
import org.springframework.boot.test.context.runner.ApplicationContextRunner
import org.springframework.context.annotation.Bean

internal class DataPoolCoreDeletionStrategyConfigurationTest {

  private val contextRunner = ApplicationContextRunner()
    .withConfiguration(UserConfigurations.of(DataPoolCoreConfiguration::class.java))

  @Test
  fun `should do lax by default`() {
    contextRunner
      .withUserConfiguration(TestMockConfiguration::class.java)
      .withPropertyValues(
        ""
      ).run {
        assertThat(it.getBean(DataPoolProperties::class.java)).isNotNull
        assertThat(it.getBean(DeletionStrategy::class.java)).isNotNull

        val properties = it.getBean(DataPoolProperties::class.java)
        assertThat(properties.deletionStrategy).isEqualTo(DeletionStrategyValue.lax)

        val deletionStrategy = it.getBean(DeletionStrategy::class.java)
        assertThat(deletionStrategy.strictMode()).isFalse()
      }
  }

  @Test
  fun `should do strict if changed`() {
    contextRunner
      .withUserConfiguration(TestMockConfiguration::class.java)
      .withPropertyValues(
        "polyflow.core.data-entry.deletion-strategy=strict"
      ).run {
        assertThat(it.getBean(DataPoolProperties::class.java)).isNotNull
        assertThat(it.getBean(DeletionStrategy::class.java)).isNotNull

        val properties = it.getBean(DataPoolProperties::class.java)
        assertThat(properties.deletionStrategy).isEqualTo(DeletionStrategyValue.strict)

        val deletionStrategy: DeletionStrategy = it.getBean(DeletionStrategy::class.java)
        assertThat(deletionStrategy.strictMode()).isTrue()
      }
  }

  @Test
  fun `should do lax if changed`() {
    contextRunner
      .withUserConfiguration(TestMockConfiguration::class.java)
      .withPropertyValues(
        "polyflow.core.data-entry.deletion-strategy=lax"
      ).run {
        assertThat(it.getBean(DataPoolProperties::class.java)).isNotNull
        assertThat(it.getBean(DeletionStrategy::class.java)).isNotNull

        val properties = it.getBean(DataPoolProperties::class.java)
        assertThat(properties.deletionStrategy).isEqualTo(DeletionStrategyValue.lax)

        val deletionStrategy: DeletionStrategy = it.getBean(DeletionStrategy::class.java)
        assertThat(deletionStrategy.strictMode()).isFalse()
      }
  }

  @Test
  fun `should not allow wrong values in deletion mode`() {

    contextRunner
      .withUserConfiguration(TestMockConfiguration::class.java)
      .withPropertyValues(
        "polyflow.core.data-entry.deletion-strategy=hugo"
      ).run {
        assertThrows<IllegalStateException> { it.getBean(DataPoolProperties::class.java) }
      }
  }

  class TestMockConfiguration {

    @Bean
    fun eventStore(): EventStore = mock()

    @Bean
    fun snapshotter(): Snapshotter = mock()
  }
}