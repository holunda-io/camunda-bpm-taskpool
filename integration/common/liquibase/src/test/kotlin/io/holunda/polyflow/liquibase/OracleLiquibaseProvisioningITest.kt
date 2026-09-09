package io.holunda.polyflow.liquibase

import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Nested
import org.testcontainers.containers.OracleContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.sql.DriverManager

@Testcontainers
internal class OracleLiquibaseProvisioningITest : AbstractLiquibaseTestFixture() {
  override fun jdbcUrl(databaseName: String) = oracle.jdbcUrl

  @Nested inner class Core : AbstractCoreLiquibaseProvisioningITest(this@OracleLiquibaseProvisioningITest, CORE_SCHEMA)
  @Nested inner class View : AbstractViewLiquibaseProvisioningITest(this@OracleLiquibaseProvisioningITest, VIEW_SCHEMA)

  companion object {

    @Container
    @JvmStatic
    val oracle = OracleContainer("gvenzl/oracle-xe:21-slim-faststart")
      .apply { withPassword(TEST_PASSWORD) }

    @BeforeAll
    @JvmStatic
    fun createSchemas() = DriverManager.getConnection(oracle.jdbcUrl, "system", TEST_PASSWORD).use { connection ->
      connection.createStatement().use { statement ->
        listOf(CORE_SCHEMA, VIEW_SCHEMA).forEach { schema ->
          statement.execute("create user $schema identified by \"$TEST_PASSWORD\"")
          statement.execute("grant connect, resource to $schema")
          statement.execute("alter user $schema quota unlimited on users")
        }
        Unit
      }
    }
  }
}
