package models.engine

import models.engine.DatabaseEngine._
import models.query.RowDataOptions
import models.schema.{ColumnType, FilterOp}

object EngineQueries {
  private[this] def parse(t: ColumnType, s: String) = EngineColumnParser.parse(t, s.trim) match {
    case Right(z) => z
    case Left(z) => z
  }

  def selectFrom(name: String, options: RowDataOptions = RowDataOptions.empty)(implicit engine: DatabaseEngine) = {
    val whereClauseAdditions = options.limit match {
      case Some(l) if engine == Oracle => options.offset match {
        case Some(o) => Some(s" rownum <= ${l + o} and rownum > $o")
        case None => None
      }
      case Some(_) => None
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
      case Some(_) => throw new IllegalStateException(s"No limit support for engine [$engine].")
      case None => options.offset match {
        case Some(o) if o > 0 => s" offset $o rows"
        case _ => ""
      }
    }

    val quotedName = engine.cap.leftQuote + name + engine.cap.rightQuote

    val filterClauses = if(options.filters.isEmpty) {
      Nil
    } else {
      options.filters.map { filter =>
        val (q, v) = filter.op match {
          case FilterOp.Between =>
            val split = filter.v.split('|').toSeq
            (" " + split.map(_ => "?").mkString(" and ")) -> split.map(s => parse(filter.t, s.trim))
          case FilterOp.In =>
            val split = filter.v.split(',').toSeq
            " (" + split.map(_ => "?").mkString(", ") + ")" -> split.map(s => parse(filter.t, s))
          case FilterOp.IsNull | FilterOp.IsNotNull => "" -> Nil
          case FilterOp.Like if !filter.v.contains('%') => " ?" -> Seq(parse(filter.t, s"%${filter.v}%"))
          case FilterOp.Like => " ?" -> Seq(parse(filter.t, filter.v))
          case _ => " ?" -> Seq(parse(filter.t, filter.v))
        }
        s"${engine.cap.leftQuote}${filter.col}${engine.cap.rightQuote} ${filter.op.sqlSymbol}$q" -> v
      }
    }

    val (whereClause, values) = if(options.filters.isEmpty) {
      whereClauseAdditions.map(" where" + _).getOrElse("") -> Nil
    } else {
      val additions = whereClauseAdditions.map(" and " + _).getOrElse("")
      s" where ${filterClauses.map(_._1).mkString(" and ")}$additions" -> filterClauses.flatMap(_._2)
    }
    val orderByClause = options.orderByCol.map { orderCol =>
      val ordering = if (options.orderByAsc.contains(false)) { "desc" } else { "asc" }
      s" order by ${engine.cap.leftQuote}$orderCol${engine.cap.rightQuote} $ordering"
    }.getOrElse("")
    val sql = s"select$preColumnsClause * from $quotedName$whereClause$orderByClause$postQueryClauses"
    sql -> values
  }
}
