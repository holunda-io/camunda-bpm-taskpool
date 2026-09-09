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
const val TEST_PASSWORD = "Po1!Flow"

private val expectedReleaseTag = System.getProperty("polyflow.liquibase.release-version")
  ?.let { releaseVersion ->
    Regex("^(\\d+)\\.(\\d+)\\.\\d+(-SNAPSHOT)?$").matchEntire(releaseVersion)
      ?.let { match -> "${match.groupValues[1]}.${match.groupValues[2]}" }
      ?: error("Expected polyflow.liquibase.release-version to match X.Y.Z or X.Y.Z-SNAPSHOT, but was '$releaseVersion'")
  }
  ?: error("Missing polyflow.liquibase.release-version system property")

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
    assertThat(jdbcTemplate.queryForObject("select count(*) from DATABASECHANGELOG where ID = 'core-baseline-4.6'", Int::class.java))
      .isEqualTo(1)
    assertThat(jdbcTemplate.queryForObject("select count(*) from DATABASECHANGELOG where TAG = ?", Int::class.java, expectedReleaseTag))
      .describedAs("Checked expected release version '%s' against database tags; they do not match. Make sure to update the database version for this release.", expectedReleaseTag)
      .isGreaterThanOrEqualTo(1)
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
    assertThat(jdbcTemplate.queryForObject("select count(*) from TOKEN_ENTRY", Int::class.java)).isZero()
    assertThat(jdbcTemplate.queryForObject("select count(*) from DEAD_LETTER_ENTRY", Int::class.java)).isZero()
    assertThat(jdbcTemplate.queryForObject("select count(*) from DATABASECHANGELOG where ID = 'event-baseline-4.6'", Int::class.java))
      .isEqualTo(1)
    assertThat(jdbcTemplate.queryForObject("select count(*) from DATABASECHANGELOG where ID = 'view-baseline-4.6'", Int::class.java))
      .isEqualTo(1)
    assertThat(jdbcTemplate.queryForObject("select count(*) from DATABASECHANGELOG where TAG = ?", Int::class.java, expectedReleaseTag))
      .describedAs("Checked expected release version '%s' against database tags; they do not match. Make sure to update the database version for this release.", expectedReleaseTag)
      .isGreaterThanOrEqualTo(1)
  }
}
