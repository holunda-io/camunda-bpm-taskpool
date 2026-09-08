package io.holunda.polyflow.view.jpa

import jakarta.persistence.Persistence
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.EnabledIfSystemProperty
import java.nio.file.Files
import java.nio.file.Path

@EnabledIfSystemProperty(named = "generate-sql", matches = "true")
internal class SchemaGenerationTest {

  @Test
  fun `generates DDL for all supported databases`() {
    val outputDirectory = Path.of(System.getProperty("schema-output-directory"))

    dialects.forEach { (dialect, outputFile) ->
      val outputPath = outputDirectory.resolve(outputFile)
      Files.deleteIfExists(outputPath)

      Persistence.createEntityManagerFactory(
        "default",
        mapOf(
          "jakarta.persistence.schema-generation.database.action" to "none",
          "jakarta.persistence.schema-generation.scripts.action" to "create",
          "jakarta.persistence.schema-generation.scripts.create-target" to outputPath.toString(),
          "hibernate.boot.allow_jdbc_metadata_access" to "false",
          "hibernate.dialect" to dialect,
          "hibernate.format_sql" to "true",
          "hibernate.hbm2ddl.delimiter" to ";",
          "hibernate.physical_naming_strategy" to "org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy"
        )
      ).close()

      assertThat(outputPath)
        .exists()
        .isNotEmptyFile()
      assertThat(Files.readString(outputPath)).containsIgnoringCase("create table")
    }
  }

  private companion object {
    val dialects = mapOf(
      "org.hibernate.dialect.H2Dialect" to "h2_ddl.sql",
      "org.hibernate.dialect.SQLServerDialect" to "mssql_ddl.sql",
      "org.hibernate.dialect.PostgreSQLDialect" to "pgsql_ddl.sql"
    )
  }
}
