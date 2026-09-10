package io.holunda.polyflow.liquibase

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.boot.builder.SpringApplicationBuilder
import java.sql.DriverManager

internal class H2LiquibaseProvisioningITest : AbstractLiquibaseTestFixture() {
  override fun jdbcUrl(databaseName: String) = "jdbc:h2:mem:$databaseName;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;USER=test;PASSWORD=$TEST_PASSWORD"

  @Nested
  inner class Core : AbstractCoreLiquibaseProvisioningITest(this@H2LiquibaseProvisioningITest, CORE_DATABASE)
  @Nested
  inner class View : AbstractViewLiquibaseProvisioningITest(this@H2LiquibaseProvisioningITest, VIEW_DATABASE)

  @Test
  fun `adopts an existing core schema through the application`() {
    withApplication(CORE_DATABASE, "core") { jdbcTemplate ->
      jdbcTemplate.execute("delete from DATABASECHANGELOG")
    }

    SpringApplicationBuilder(LiquibaseTestApplication::class.java)
      .profiles("core")
      .properties(
        "test.datasource.url=${jdbcUrl(CORE_DATABASE)}",
        "polyflow.liquibase.adoption.enabled=true"
      )
      .run()

    DriverManager.getConnection(jdbcUrl(CORE_DATABASE)).use { connection ->
      connection.createStatement().use { statement ->
        statement.executeQuery("select count(*) from DATABASECHANGELOG where ID = 'core-baseline-4.6'").use { result ->
          result.next()
          assertThat(result.getInt(1)).isEqualTo(1)
        }
        statement.executeQuery("select count(*) from DATABASECHANGELOG where TAG = '4.6'").use { result ->
          result.next()
          assertThat(result.getInt(1)).isGreaterThanOrEqualTo(1)
        }
      }
    }
  }
}
