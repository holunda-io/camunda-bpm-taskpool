package io.holunda.polyflow.liquibase

import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Nested
import org.testcontainers.containers.MSSQLServerContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@Testcontainers
internal class MsSqlLiquibaseProvisioningITest : AbstractLiquibaseTestFixture() {
  override fun jdbcUrl(databaseName: String) =
    "jdbc:sqlserver://${msSql.host}:${msSql.getMappedPort(1433)};databaseName=$databaseName;encrypt=false;trustServerCertificate=true"

  @Nested inner class Core : AbstractCoreLiquibaseProvisioningITest(this@MsSqlLiquibaseProvisioningITest, CORE_DATABASE)
  @Nested inner class View : AbstractViewLiquibaseProvisioningITest(this@MsSqlLiquibaseProvisioningITest, VIEW_DATABASE)

  companion object {
    @Container @JvmStatic val msSql = MSSQLServerContainer<Nothing>("mcr.microsoft.com/mssql/server:2022-CU17-ubuntu-22.04").apply {
      acceptLicense()
      withPassword(TEST_PASSWORD)
    }
    @BeforeAll @JvmStatic fun createDatabases() = msSql.createConnection("").use { connection ->
      connection.createStatement().use { statement ->
        statement.execute("create database $CORE_DATABASE")
        statement.execute("create database $VIEW_DATABASE")
        statement.execute("create login test with password = '$TEST_PASSWORD'")
        statement.execute("use $CORE_DATABASE; create user test for login test; alter role db_owner add member test")
        statement.execute("use $VIEW_DATABASE; create user test for login test; alter role db_owner add member test")
        Unit
      }
    }
  }
}
