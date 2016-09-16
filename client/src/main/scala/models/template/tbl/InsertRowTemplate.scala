package models.template.tbl

import models.schema.Column
import utils.Messages

import scala.util.control.NonFatal
import scalatags.Text.all._

object InsertRowTemplate {
  private[this] def forColumn(col: Column) = {
    val v = try {
      col.columnType.fromString(col.defaultValue.getOrElse("")).toString
    } catch {
      case NonFatal(x) => ""
    }
    div(cls := "input-field col s12")(
      div(cls := "insert-row-error", id := s"insert-row-error-${col.name}", style := ""),
      input(
        placeholder := (col.notNull match {
          case true if col.defaultValue.isEmpty => col.columnType + ", required"
          case _ => col.columnType.toString
        }),
        id := s"insert-row-input-${col.name}",
        cls := s"insert-row-input",
        `type` := "text",
        data("col") := col.name,
        value := v
      ),
      label(`for` := s"insert-row-input-${col.name}", cls := "active")(col.name)
    )
  }

  def forColumns(name: String, cols: Seq[Column]) = div(
    h5(Messages("query.insert")),
    div(cls := "insert-row-container row")(cols.map(c => forColumn(c)): _*)
  )
}
