package io.holunda.polyflow.view.jpa.itest

import org.hibernate.dialect.H2Dialect
import java.sql.Types

class FixedH2Dialect : H2Dialect() {
  init {
    // registerColumnType(Types.VARBINARY, "bytea")
    registerColumnType(Types.BLOB, "bytea")
    // registerColumnType(Types.LONGVARBINARY, "bytea");
  }

}
