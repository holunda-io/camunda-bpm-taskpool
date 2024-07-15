package io.holunda.polyflow.view.jpa.itest

import org.hibernate.boot.model.TypeContributions
import org.hibernate.dialect.MariaDBDialect
import org.hibernate.dialect.PostgreSQLDialect
import org.hibernate.engine.jdbc.dialect.spi.DialectResolutionInfo
import org.hibernate.service.ServiceRegistry
import org.hibernate.type.SqlTypes
import org.hibernate.type.descriptor.jdbc.BinaryJdbcType
import org.hibernate.type.descriptor.sql.internal.DdlTypeImpl
import java.sql.Types

@Suppress("unused") // used in application.yaml
class NoToastMariaDbSQLDialect(info: DialectResolutionInfo) : MariaDBDialect(info) {

  override fun registerColumnTypes(typeContributions: TypeContributions, serviceRegistry: ServiceRegistry) {
    super.registerColumnTypes(typeContributions, serviceRegistry)
    val ddlTypeRegistry = typeContributions.typeConfiguration.ddlTypeRegistry
    ddlTypeRegistry.addDescriptor(DdlTypeImpl(Types.BLOB, "bytea", this))
    ddlTypeRegistry.addDescriptor(DdlTypeImpl(Types.CLOB, "bytea", this))
    ddlTypeRegistry.addDescriptor(DdlTypeImpl(Types.NCLOB, "bytea", this))
  }

  override fun columnType(sqlTypeCode: Int): String {
    return when (sqlTypeCode) {
      SqlTypes.BLOB -> "bytea"
      else -> super.columnType(sqlTypeCode)
    }
  }

  override fun castType(sqlTypeCode: Int): String {
    return when (sqlTypeCode) {
      SqlTypes.BLOB -> "bytea"
      else -> super.castType(sqlTypeCode)
    }
  }

  override fun contributeTypes(typeContributions: TypeContributions, serviceRegistry: ServiceRegistry) {
    super.contributeTypes(typeContributions, serviceRegistry)
    val jdbcTypeRegistry = typeContributions.typeConfiguration
      .jdbcTypeRegistry
    jdbcTypeRegistry.addDescriptor(Types.BLOB, BinaryJdbcType.INSTANCE)
  }
}
