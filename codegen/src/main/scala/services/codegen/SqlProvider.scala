package services.codegen

import models.codegen.Engine
import models.codegen.Engine._

object SqlProvider {
  def varchar(implicit engine: Engine) = engine match {
    case PostgreSQL => s"character varying"
    case H2 | MySQL => s"varchar"
  }
}
