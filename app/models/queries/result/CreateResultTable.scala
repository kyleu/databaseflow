package models.queries.result

import java.util.UUID

import models.database.Statement
import models.engine.DatabaseEngine
import models.query.QueryResult

case class CreateResultTable(resultId: UUID, columns: Seq[QueryResult.Col])(implicit engine: DatabaseEngine) extends Statement {
  val tableName = s"result_${resultId.toString.replaceAllLiterally("-", "")}"

  override def sql = {
    val quotedName = engine.leftQuoteIdentifier + tableName + engine.rightQuoteIdentifier
    val rowNumCol = "row_num integer not null"

    val pkName = engine.leftQuoteIdentifier + tableName + "_pk" + engine.rightQuoteIdentifier
    val pkConstraint = s"constraint $pkName primary key (row_num)"

    s"""create table $quotedName (
      $rowNumCol,
      $pkConstraint
    )""".stripMargin
  }
}
