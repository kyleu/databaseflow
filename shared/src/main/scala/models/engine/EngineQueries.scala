package models.engine

import models.engine.rdbms._

object EngineQueries {
  def selectFrom(name: String, limit: Option[Int] = None, offset: Option[Int] = None)(implicit engine: DatabaseEngine) = {
    val preColumns = limit match {
      case Some(l) if engine == SqlServer => "top 1 "
      case _ => ""
    }
    val whereClauseAdditions = limit match {
      case Some(l) if engine == Oracle => Some(" rownum <= " + l)
      case _ => None
    }
    val postQueryClauses = limit match {
      case Some(l) if engine == H2 || engine == MySQL || engine == PostgreSQL => " limit " + l
      case _ => ""
    }

    val quotedName = engine.leftQuoteIdentifier + name + engine.rightQuoteIdentifier
    val whereClause = whereClauseAdditions.map(" where " + _).getOrElse("")
    val sql = s"select $preColumns* from $quotedName$whereClause$postQueryClauses"
    sql
  }
}
