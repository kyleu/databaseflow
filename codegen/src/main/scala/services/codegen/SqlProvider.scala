package services.codegen

import models.codegen.Engine
import models.codegen.Engine._

object SqlProvider {
  def varchar(implicit engine: Engine) = engine match {
    case PostgreSQL => "character varying"
    case _ => "varchar"
  }

  def text(implicit engine: Engine) = engine match {
    case H2 => "clob"
    case _ => "text"
  }
}
