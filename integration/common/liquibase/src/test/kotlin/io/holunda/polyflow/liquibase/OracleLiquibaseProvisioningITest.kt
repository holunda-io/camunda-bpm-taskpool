package io.holunda.polyflow.liquibase

import org.junit.jupiter.api.Nested
import org.testcontainers.containers.OracleContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@Testcontainers
internal class OracleLiquibaseProvisioningITest : AbstractLiquibaseTestFixture() {
  override fun jdbcUrl(databaseName: String) = oracle.jdbcUrl

  @Nested inner class Core : AbstractCoreLiquibaseProvisioningITest(this@OracleLiquibaseProvisioningITest, CORE_DATABASE)
  @Nested inner class View : AbstractViewLiquibaseProvisioningITest(this@OracleLiquibaseProvisioningITest, VIEW_DATABASE)

  companion object {

    @Container
    @JvmStatic
    val oracle = OracleContainer("gvenzl/oracle-xe:21-slim-faststart")
      .apply { withPassword(TEST_PASSWORD) }

  }
}
