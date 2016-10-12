package models.template.tbl

import models.query.QueryResult

import scalatags.Text.all._

object RowDetailTemplate {
  def forData(data: Seq[(QueryResult.Col, String)]) = div(cls := "row-detail-container")(
    table(cls := "data-table bordered highlight")(
      tbody(data.map { d =>
        tr(
          td(d._1.name),
          td(d._2)
        )
      }: _*)
    )
  )
}
