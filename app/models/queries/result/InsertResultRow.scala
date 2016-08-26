package models.queries.result

import models.database.Statement
import models.engine.DatabaseEngine

case class InsertResultRow(tableName: String, columns: Seq[String], override val values: Seq[Any])(implicit engine: DatabaseEngine) extends Statement {
  override val sql = {
    val quotedName = engine.cap.leftQuote + tableName + engine.cap.rightQuote
    s"""insert into $quotedName (
      ${columns.map(engine.cap.leftQuote + _ + engine.cap.rightQuote).mkString(", ")}
    ) values (
      ${values.map(y => "?").mkString(", ")}
    )
    """
  }
}
