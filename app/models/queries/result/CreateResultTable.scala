package models.queries.result

import java.util.UUID

import models.database.Statement
import models.engine.DatabaseEngine
import models.engine.rdbms.PostgreSQL
import models.schema.ColumnType
import models.schema.ColumnType._

object CreateResultTable {
  def columnFor(name: String, t: ColumnType)(implicit engine: DatabaseEngine) = {
    val colDeclaration = t match {
      case StringType => engine match {
        case PostgreSQL => "character varying(8192)"
        case _ => "varchar(8192)"
      }
      case IntegerType => "integer"
      case TimestampType => "timestamp"
      case x => throw new IllegalStateException(s"Unhandled column type [$t].")
    }
    s"${engine.leftQuoteIdentifier}$name${engine.rightQuoteIdentifier} $colDeclaration"
  }
}

case class CreateResultTable(resultId: UUID, columns: Seq[(String, ColumnType)])(implicit engine: DatabaseEngine) extends Statement {
  val tableName = s"result_${resultId.toString.replaceAllLiterally("-", "")}"

  override def sql = {
    val quotedName = engine.leftQuoteIdentifier + tableName + engine.rightQuoteIdentifier
    val rowNumCol = CreateResultTable.columnFor("row_num", IntegerType) + " not null"

    val columnStatements = columns.map(x => CreateResultTable.columnFor(x._1, x._2))

    val pkName = engine.leftQuoteIdentifier + tableName + "_pk" + engine.rightQuoteIdentifier
    val pkConstraint = s"""constraint $pkName primary key (\"row_num\")"""

    s"""create table $quotedName (
      $rowNumCol,
      ${columnStatements.mkString(",\n      ")},
      $pkConstraint
    )"""
  }
}
