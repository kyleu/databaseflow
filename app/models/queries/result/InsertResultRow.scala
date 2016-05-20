package models.queries.result

import models.database.Statement
import models.engine.DatabaseEngine

case class InsertResultRow(tableName: String, columns: Seq[String], override val values: Seq[Any])(implicit engine: DatabaseEngine) extends Statement {
  override def sql = {
    val quotedName = engine.leftQuoteIdentifier + tableName + engine.rightQuoteIdentifier

    s"""insert into $quotedName (
      ${columns.map(engine.leftQuoteIdentifier + _ + engine.rightQuoteIdentifier).mkString(", ")}
    ) values (
      ${values.map(y => "?").mkString(", ")}
    )
    """
  }
}
