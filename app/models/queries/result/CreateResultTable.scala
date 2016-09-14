package models.queries.result

import java.util.UUID

import models.database.Statement
import models.engine.DatabaseEngine
import models.engine.DatabaseEngine.PostgreSQL
import models.query.QueryResult
import models.schema.ColumnType._

object CreateResultTable {
  def columnFor(col: QueryResult.Col)(implicit engine: DatabaseEngine) = {
    val colDeclaration = col.t match {
      case StringType if col.precision.exists(_ > 32000) => "text"
      case StringType => engine match {
        case PostgreSQL => s"character varying(${col.precision.getOrElse(32000)})"
        case _ => s"varchar(${col.precision.getOrElse(32000)})"
      }
      case BigDecimalType => col.precision match {
        case Some(pr) => col.scale match {
          case Some(sc) => s"decimal($pr, $sc)"
          case None => s"decimal($pr)"
        }
        case None => "decimal"
      }
      case BooleanType => "boolean"
      case ByteType => engine match {
        case PostgreSQL => "smallint"
        case _ => "tinyint"
      }
      case ShortType => "smallint"
      case IntegerType => "integer"
      case LongType => "bigint"
      case FloatType => "real"
      case DoubleType => "double precision"
      case ByteArrayType => "bytea"
      case DateType => "date"
      case TimeType => "time"
      case TimestampType => "timestamp"
      case RefType => "text"
      case XmlType => "text"
      case UuidType => "uuid"

      case NullType => throw new IllegalArgumentException("Cannot support null column types.")
      case ObjectType => "text"
      case StructType => "text"
      case ArrayType => "text"

      case UnknownType => "text"

      case x => throw new IllegalStateException(s"Unhandled column type [${col.t}].")
    }
    s"${engine.cap.leftQuote}${col.name}${engine.cap.rightQuote} $colDeclaration"
  }
}

case class CreateResultTable(tableName: String, columns: Seq[QueryResult.Col])(implicit engine: DatabaseEngine) extends Statement {
  override val sql = {
    val quotedName = engine.cap.leftQuote + tableName + engine.cap.rightQuote
    val rowNumCol = if (columns.exists(_.name == "#")) {
      ""
    } else {
      s"${engine.cap.leftQuote}#${engine.cap.rightQuote} integer not null,"
    }

    val columnStatements = columns.map(x => CreateResultTable.columnFor(x))

    val pkName = engine.cap.leftQuote + tableName + "_pk" + engine.cap.rightQuote
    val pkConstraint = s"""constraint $pkName primary key (\"#\")"""

    s"""create table $quotedName (
      $rowNumCol
      ${columnStatements.mkString(",\n      ")},
      $pkConstraint
    )"""
  }
}
