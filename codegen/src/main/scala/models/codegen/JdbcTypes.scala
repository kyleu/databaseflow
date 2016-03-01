package models.codegen

import java.sql.Types

object JdbcTypes {
  val types = classOf[Types].getFields.map(x => x.getName -> x.get(None.orNull).asInstanceOf[Int]).toSeq.sortBy(_._1)
}
