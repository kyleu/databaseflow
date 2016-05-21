package models.queries.result

import java.util.UUID

import models.database.Statement
import models.engine.DatabaseEngine
import models.engine.rdbms.PostgreSQL
import models.query.QueryResult
import models.schema.ColumnType._

object CreateResultTable {
  def columnFor(col: QueryResult.Col)(implicit engine: DatabaseEngine) = {
    val colDeclaration = col.t match {
      case StringType => engine match {
        case PostgreSQL => s"character varying(${col.precision})"
        case _ => s"varchar(${col.precision})"
      }
      case BigDecimalType => s"decimal(${col.precision}, ${col.scale})"
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
      case ObjectType => throw new IllegalArgumentException("Cannot support object column types.")
      case StructType => throw new IllegalArgumentException("Cannot support struct column types.")
      case ArrayType => "array"

      case UnknownType => throw new IllegalArgumentException("Cannot support unknown column types.")

      case x => throw new IllegalStateException(s"Unhandled column type [${col.t}].")
    }
    s"${engine.leftQuoteIdentifier}${col.name}${engine.rightQuoteIdentifier} $colDeclaration"
  }
}

case class CreateResultTable(resultId: UUID, columns: Seq[QueryResult.Col])(implicit engine: DatabaseEngine) extends Statement {
  val tableName = s"result_${resultId.toString.replaceAllLiterally("-", "")}"

  override def sql = {
    val quotedName = engine.leftQuoteIdentifier + tableName + engine.rightQuoteIdentifier
    val rowNumCol = s"${engine.leftQuoteIdentifier}row_num${engine.rightQuoteIdentifier} integer not null"

    val columnStatements = columns.map(x => CreateResultTable.columnFor(x))

    val pkName = engine.leftQuoteIdentifier + tableName + "_pk" + engine.rightQuoteIdentifier
    val pkConstraint = s"""constraint $pkName primary key (\"row_num\")"""

    s"""create table $quotedName (
      $rowNumCol,
      ${columnStatements.mkString(",\n      ")},
      $pkConstraint
    )"""
  }
}
