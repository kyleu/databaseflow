package services.codegen

import models.codegen.Engine
import models.codegen.Engine._
import models.codegen.dialect.SQLiteDialect
import org.hibernate.dialect.Dialect

object DialectProvider {
  case class FlowDialect(d: Option[Dialect]) {
    def functions() = d.map(CapabilitiesProvider.getFunctions).getOrElse(throw new IllegalStateException("Missing functions."))
    def types() = d.map(CapabilitiesProvider.getTypes).getOrElse(throw new IllegalStateException("Missing types."))
  }

  def dialectFor(engine: Engine) = engine match {
    case DB2 => FlowDialect(Some(new org.hibernate.dialect.DB2Dialect()))
    case H2 => FlowDialect(Some(new org.hibernate.dialect.H2Dialect()))
    case Informix => FlowDialect(Some(new org.hibernate.dialect.InformixDialect()))
    case MySQL => FlowDialect(Some(new org.hibernate.dialect.MySQL57InnoDBDialect()))
    case Oracle => FlowDialect(Some(new org.hibernate.dialect.Oracle12cDialect()))
    case PostgreSQL => FlowDialect(Some(new org.hibernate.dialect.PostgreSQL94Dialect()))
    case SQLServer => FlowDialect(Some(new org.hibernate.dialect.SQLServer2012Dialect()))
    case SQLite => SQLiteDialect
  }
}
