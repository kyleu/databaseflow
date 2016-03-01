package services.codegen

import models.codegen.Engine
import models.codegen.Engine._

object DialectProvider {
  def dialectFor(engine: Engine) = engine match {
    case PostgreSQL => new org.hibernate.dialect.PostgreSQL94Dialect()
    case MySQL => new org.hibernate.dialect.MySQL57InnoDBDialect()
    case H2 => new org.hibernate.dialect.H2Dialect()
  }
}
