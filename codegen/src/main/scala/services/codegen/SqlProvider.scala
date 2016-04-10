package services.codegen

import models.codegen.Engine
import models.codegen.Engine._

object SqlProvider {
  def leftQuoteIdentifier(implicit engine: Engine) = engine match {
    case MySQL => "`"
    case SqlServer => "["
    case _ => "\\\""
  }

  def rightQuoteIdentifier(implicit engine: Engine) = engine match {
    case MySQL => "`"
    case SqlServer => "]"
    case _ => "\\\""
  }
}
