package services.codegen

import models.codegen.Engine
import models.codegen.Engine._

object DialectProvider {
  def dialectFor(engine: Engine) = engine match {
    case DB2 => new org.hibernate.dialect.DB2Dialect()
    case H2 => new org.hibernate.dialect.H2Dialect()
    case Informix => new org.hibernate.dialect.InformixDialect()
    case MySQL => new org.hibernate.dialect.MySQL57InnoDBDialect()
    case Oracle => new org.hibernate.dialect.Oracle12cDialect()
    case PostgreSQL => new org.hibernate.dialect.PostgreSQL94Dialect()
    case SqlServer => new org.hibernate.dialect.SQLServer2012Dialect()
  }
}
