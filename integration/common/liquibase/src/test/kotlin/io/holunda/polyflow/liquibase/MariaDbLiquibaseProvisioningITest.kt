package io.holunda.polyflow.liquibase

import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Nested
import org.testcontainers.containers.MariaDBContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.sql.DriverManager

@Testcontainers
internal class MariaDbLiquibaseProvisioningITest : AbstractLiquibaseTestFixture() {
  override fun jdbcUrl(databaseName: String) = mariaDb.getJdbcUrl().replace("/$TEST_DATABASE", "/$databaseName")

  @Nested inner class Core : AbstractCoreLiquibaseProvisioningITest(this@MariaDbLiquibaseProvisioningITest, CORE_DATABASE)
  @Nested inner class View : AbstractViewLiquibaseProvisioningITest(this@MariaDbLiquibaseProvisioningITest, VIEW_DATABASE)

  companion object {
    @Container @JvmStatic val mariaDb = MariaDBContainer<Nothing>("mariadb:11.4").apply {
      withDatabaseName(TEST_DATABASE)
      withPassword(TEST_PASSWORD)
    }
    @BeforeAll @JvmStatic fun createDatabases() = DriverManager.getConnection(mariaDb.getJdbcUrl(), "root", mariaDb.password).use { connection ->
      connection.createStatement().use { statement ->
        statement.execute("create database $CORE_DATABASE")
        statement.execute("create database $VIEW_DATABASE")
        statement.execute("grant all privileges on $CORE_DATABASE.* to '${mariaDb.username}'@'%'")
        statement.execute("grant all privileges on $VIEW_DATABASE.* to '${mariaDb.username}'@'%'")
        Unit
      }
    }
  }
}
