package models.engine

import models.engine.rdbms._
import models.query.RowDataOptions

object EngineQueries {
  def selectFrom(name: String, options: RowDataOptions)(implicit engine: DatabaseEngine) = {
    val whereClauseAdditions = options.limit match {
      case Some(l) if engine == Oracle => options.offset match {
        case Some(o) => Some(s" rownum <= ${l + o} and rownum > $o")
        case None => Some(s" rownum <= $l")
      }
      case Some(l) => None
      case None => options.offset match {
        case Some(o) => Some(s" rownum > $o")
        case None => None
      }
    }
    val postQueryClauses = options.limit match {
      case Some(l) if engine == H2 || engine == MySQL || engine == PostgreSQL => options.offset match {
        case Some(o) => s" limit $l offset $o"
        case None => s" limit $l"
      }
      case Some(l) if engine == SqlServer => options.offset match {
        case Some(o) => s" offset $o rows fetch next $l rows only"
        case None => s" fetch next $l rows only"
      }
      case Some(e) => ""
      case None => options.offset match {
        case Some(o) => s" offset $o rows"
        case None => ""
      }
    }

    val quotedName = engine.leftQuoteIdentifier + name + engine.rightQuoteIdentifier
    val whereClause = options.filterCol match {
      case Some(col) =>
        val op = options.filterOp.getOrElse("=")
        val fVal = options.filterVal.getOrElse("?")
        s" where $col $op '$fVal'" + whereClauseAdditions.map(" and " + _).getOrElse("")
      case None => whereClauseAdditions.map(" where" + _).getOrElse("")
    }
    val orderByClause = options.orderByCol.map { orderCol =>
      val ordering = if (options.orderByAsc.contains(false)) { "desc" } else { "asc" }
      s" order by ${engine.leftQuoteIdentifier}$orderCol${engine.rightQuoteIdentifier} $ordering"
    }.getOrElse("")
    val sql = s"select * from $quotedName$whereClause$orderByClause$postQueryClauses"
    sql
  }
}
