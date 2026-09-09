package io.holunda.polyflow.liquibase

import org.junit.jupiter.api.Nested

internal class H2LiquibaseProvisioningITest : AbstractLiquibaseTestFixture() {
  override fun jdbcUrl(databaseName: String) = "jdbc:h2:mem:$databaseName;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;USER=test;PASSWORD=$TEST_PASSWORD"

  @Nested
  inner class Core : AbstractCoreLiquibaseProvisioningITest(this@H2LiquibaseProvisioningITest, CORE_DATABASE)
  @Nested
  inner class View : AbstractViewLiquibaseProvisioningITest(this@H2LiquibaseProvisioningITest, VIEW_DATABASE)

}
