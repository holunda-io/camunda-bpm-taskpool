package io.holunda.polyflow.liquibase.autoconfigure

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.autoconfigure.AutoConfigurations
import org.springframework.boot.test.context.runner.ApplicationContextRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.jdbc.datasource.DriverManagerDataSource
import javax.sql.DataSource

class PolyflowLiquibaseAdoptionAutoConfigurationTest {

  private val contextRunner = ApplicationContextRunner()
    .withConfiguration(AutoConfigurations.of(PolyflowLiquibaseAdoptionAutoConfiguration::class.java))
    .withUserConfiguration(DataSourceConfiguration::class.java)

  @Test
  fun `creates the one-shot adoption runner only when explicitly enabled`() {
    contextRunner
      .withPropertyValues("polyflow.liquibase.adoption.enabled=true")
      .run { context ->
        assertThat(context).hasSingleBean(ApplicationRunner::class.java)
      }
  }

  @Test
  fun `does not create an adoption runner by default`() {
    contextRunner.run { context ->
      assertThat(context).doesNotHaveBean(ApplicationRunner::class.java)
    }
  }

  @Configuration(proxyBeanMethods = false)
  private class DataSourceConfiguration {
    @Bean
    fun dataSource(): DataSource = DriverManagerDataSource("jdbc:h2:mem:polyflow_liquibase_adoption_test")
  }
}
