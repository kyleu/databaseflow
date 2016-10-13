package models.engine

import models.engine.DatabaseEngine._
import models.query.{ColumnValueParser, RowDataOptions}
import models.schema.{ColumnType, FilterOp}

object EngineQueries {
  private[this] def parse(t: ColumnType, s: String) = ColumnValueParser.parse(t, s.trim) match {
    case Right(z) => z
    case Left(z) => z
  }

  def selectFrom(name: String, options: RowDataOptions = RowDataOptions.empty)(implicit engine: DatabaseEngine) = {
    val whereClauseAdditions = options.limit match {
      case Some(l) if engine == Oracle => options.offset match {
        case Some(o) => Some(s" rownum <= ${l + o} and rownum > $o")
        case None => None
      }
      case Some(l) => None
      case None => options.offset match {
        case Some(o) if o > 0 => Some(s" rownum > $o")
        case _ => None
      }
    }
    val preColumnsClause = options.offset match {
      case Some(o) if engine == Informix => s" SKIP $o"
      case _ => ""
    }
    val postQueryClauses = options.limit match {
      case Some(l) if engine == H2 || engine == MySQL || engine == PostgreSQL || engine == SQLite => options.offset match {
        case Some(o) if o > 0 => s" limit $l offset $o"
        case _ => s" limit $l"
      }
      case Some(l) if engine == Informix => s" limit $l"
      case Some(l) if engine == SQLServer => options.offset match {
        case Some(o) => s" offset $o rows fetch next $l rows only"
        case None => s" offset 0 rows fetch next $l rows only"
      }
      case Some(e) => throw new IllegalStateException(s"No limit support for engine [$engine].")
      case None => options.offset match {
        case Some(o) if o > 0 => s" offset $o rows"
        case _ => ""
      }
    }

    val quotedName = engine.cap.leftQuote + name + engine.cap.rightQuote
    val (whereClause, values) = options.filterCol match {
      case Some(col) =>
        val t = options.filterType.getOrElse(ColumnType.StringType)
        val op = options.filterOp.getOrElse(FilterOp.Equal)
        val fVal = options.filterVal.getOrElse("?")
        val (q, v) = op match {
          case FilterOp.Between =>
            val split = fVal.split('|').toSeq
            (" " + split.map(s => "?").mkString(" and ")) -> split.map(s => parse(t, s.trim))
          case FilterOp.In =>
            val split = fVal.split(',').toSeq
            " (" + split.map(s => "?").mkString(", ") + ")" -> split.map(s => parse(t, s))
          case FilterOp.IsNull | FilterOp.IsNotNull => "" -> Nil
          case FilterOp.Like if !fVal.contains('%') => s" ?" -> Seq(parse(t, s"%$fVal%"))
          case _ => s" ?" -> Seq(fVal)
        }
        val additions = whereClauseAdditions.map(" and " + _).getOrElse("")
        s" where ${engine.cap.leftQuote}$col${engine.cap.rightQuote} ${op.sqlSymbol}$q$additions" -> v
      case None => whereClauseAdditions.map(" where" + _).getOrElse("") -> Nil
    }
    val orderByClause = options.orderByCol.map { orderCol =>
      val ordering = if (options.orderByAsc.contains(false)) { "desc" } else { "asc" }
      s" order by ${engine.cap.leftQuote}$orderCol${engine.cap.rightQuote} $ordering"
    }.getOrElse("")
    val sql = s"select$preColumnsClause * from $quotedName$whereClause$orderByClause$postQueryClauses"
    sql -> values
  }
}
