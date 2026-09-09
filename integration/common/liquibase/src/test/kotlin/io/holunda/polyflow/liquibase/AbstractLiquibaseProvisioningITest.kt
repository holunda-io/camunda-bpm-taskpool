package io.holunda.polyflow.liquibase

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.getBean
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.jdbc.core.JdbcTemplate


const val TEST_DATABASE = "polyflow_liquibase"
const val CORE_DATABASE = "polyflow_liquibase_core"
const val VIEW_DATABASE = "polyflow_liquibase_view"
const val CORE_SCHEMA = "polyflow_core"
const val VIEW_SCHEMA = "polyflow_view"
const val TEST_PASSWORD = "Po1!Flow"

abstract class AbstractLiquibaseTestFixture {

  protected abstract fun jdbcUrl(databaseName: String): String
  fun withApplication(databaseName: String, profile: String, assertion: (JdbcTemplate) -> Unit) {
    SpringApplicationBuilder(LiquibaseTestApplication::class.java)
      .profiles(profile)
      .properties(
        "test.datasource.url=${jdbcUrl(databaseName)}",
      )
      .run()
      .use { context: ConfigurableApplicationContext -> assertion(context.getBean<JdbcTemplate>()) }
  }
}

abstract class AbstractCoreLiquibaseProvisioningITest(
  private val fixture: AbstractLiquibaseTestFixture,
  private val databaseName: String,
) {
  @Test
  fun `provisions the core schema through Liquibase`() = fixture.withApplication(databaseName, "core") { jdbcTemplate ->
    assertThat(jdbcTemplate.queryForObject("select count(*) from DOMAIN_EVENT_ENTRY", Int::class.java)).isZero()
    assertThat(jdbcTemplate.queryForObject("select count(*) from DATABASECHANGELOG where ID = 'core-baseline-4.6.2'", Int::class.java))
      .isEqualTo(1)
  }
}

abstract class AbstractViewLiquibaseProvisioningITest(
  private val fixture: AbstractLiquibaseTestFixture,
  private val databaseName: String,
) {
  @Test
  fun `provisions the view schema through Liquibase`() = fixture.withApplication(databaseName, "view") { jdbcTemplate ->
    assertThat(jdbcTemplate.queryForObject("select count(*) from PLF_TASK", Int::class.java)).isZero()
    assertThat(jdbcTemplate.queryForObject("select count(*) from PLF_VIEW_DATA_ENTRY_PAYLOAD", Int::class.java)).isZero()
    assertThat(jdbcTemplate.queryForObject("select count(*) from DATABASECHANGELOG where ID = 'view-baseline-4.6.2'", Int::class.java))
      .isEqualTo(1)
  }
}
