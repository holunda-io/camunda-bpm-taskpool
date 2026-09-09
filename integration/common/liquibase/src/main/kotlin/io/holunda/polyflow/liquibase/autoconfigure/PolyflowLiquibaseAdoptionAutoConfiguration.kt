package io.holunda.polyflow.liquibase.autoconfigure

import liquibase.Contexts
import liquibase.LabelExpression
import liquibase.Liquibase
import liquibase.database.DatabaseFactory
import liquibase.database.jvm.JdbcConnection
import liquibase.integration.spring.SpringResourceAccessor
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.autoconfigure.liquibase.LiquibaseProperties
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.core.io.ResourceLoader
import java.util.Properties
import javax.sql.DataSource

/**
 * Explicit, one-shot adoption of a verified existing Polyflow schema.
 *
 * The regular Spring Boot Liquibase integration is disabled by
 * [PolyflowLiquibaseAdoptionEnvironmentPostProcessor] before this runner is
 * created. This runner records the configured changelog without executing its
 * changes and records the Polyflow release tag before closing the application.
 */
@AutoConfiguration
@AutoConfigureAfter(DataSourceAutoConfiguration::class)
@ConditionalOnClass(Liquibase::class)
@ConditionalOnBean(DataSource::class)
@ConditionalOnProperty(
  prefix = "polyflow.liquibase.adoption",
  name = ["enabled"],
  havingValue = "true"
)
@EnableConfigurationProperties(LiquibaseProperties::class)
class PolyflowLiquibaseAdoptionAutoConfiguration {

  /**
   * Creates the one-shot runner that synchronizes and tags a verified schema
   * before closing the application context.
   */
  @Bean
  fun polyflowLiquibaseAdoptionRunner(
    dataSource: DataSource,
    liquibaseProperties: LiquibaseProperties,
    resourceLoader: ResourceLoader,
    applicationContext: ConfigurableApplicationContext
  ): ApplicationRunner = ApplicationRunner {
    PolyflowLiquibaseSchemaAdopter(dataSource, liquibaseProperties, resourceLoader).adopt()
    applicationContext.close()
  }
}

internal class PolyflowLiquibaseSchemaAdopter(
  private val dataSource: DataSource,
  private val properties: LiquibaseProperties,
  private val resourceLoader: ResourceLoader
) {

  fun adopt() {
    dataSource.connection.use { connection ->
      val database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(JdbcConnection(connection))
      configure(database)
      Liquibase(properties.changeLog, SpringResourceAccessor(resourceLoader), database).use { liquibase ->
        properties.parameters?.forEach(liquibase::setChangeLogParameter)
        val contexts = Contexts(*(properties.contexts ?: emptyList()).toTypedArray())
        val labels = LabelExpression(*(properties.labelFilter ?: emptyList()).toTypedArray())
        liquibase.changeLogSync(contexts, labels)
        liquibase.tag(PolyflowLiquibaseModuleVersion.releaseTag)
      }
    }
  }

  private fun configure(database: liquibase.database.Database) {
    properties.defaultSchema?.let(database::setDefaultSchemaName)
    properties.liquibaseSchema?.let(database::setLiquibaseSchemaName)
    properties.liquibaseTablespace?.let(database::setLiquibaseTablespaceName)
    properties.databaseChangeLogTable?.let(database::setDatabaseChangeLogTableName)
    properties.databaseChangeLogLockTable?.let(database::setDatabaseChangeLogLockTableName)
  }
}

internal object PolyflowLiquibaseModuleVersion {
  private const val BUILD_PROPERTIES = "META-INF/polyflow-liquibase-build.properties"
  private val RELEASE_VERSION = Regex("^(\\d+)\\.(\\d+)\\.\\d+(-SNAPSHOT)?$")

  val releaseTag: String by lazy {
    val version = Properties().also { properties ->
      PolyflowLiquibaseModuleVersion::class.java.classLoader.getResourceAsStream(BUILD_PROPERTIES)?.use(properties::load)
        ?: error("Missing $BUILD_PROPERTIES in the polyflow-liquibase artifact")
    }.getProperty("version")
      ?: error("Missing version in $BUILD_PROPERTIES")

    RELEASE_VERSION.matchEntire(version)?.let { match -> "${match.groupValues[1]}.${match.groupValues[2]}" }
      ?: error("Expected polyflow-liquibase module version X.Y.Z or X.Y.Z-SNAPSHOT, but was '$version'")
  }
}
