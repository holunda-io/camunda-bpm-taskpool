package io.holunda.polyflow.liquibase

import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Nested
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@Testcontainers
internal class PostgresLiquibaseProvisioningITest : AbstractLiquibaseTestFixture() {
  override fun jdbcUrl(databaseName: String) = postgres.getJdbcUrl().replace("/$TEST_DATABASE?", "/$databaseName?")

  @Nested inner class Core : AbstractCoreLiquibaseProvisioningITest(this@PostgresLiquibaseProvisioningITest, CORE_DATABASE)
  @Nested inner class View : AbstractViewLiquibaseProvisioningITest(this@PostgresLiquibaseProvisioningITest, VIEW_DATABASE)

  companion object {
    @Container @JvmStatic val postgres = PostgreSQLContainer<Nothing>("postgres:17-alpine").apply {
      withDatabaseName(TEST_DATABASE)
      withPassword(TEST_PASSWORD)
    }
    @BeforeAll @JvmStatic fun createDatabases() = postgres.createConnection("").use { connection ->
      connection.createStatement().use { statement ->
        statement.execute("create database $CORE_DATABASE")
        statement.execute("create database $VIEW_DATABASE")
        Unit
      }
    }
  }
}
