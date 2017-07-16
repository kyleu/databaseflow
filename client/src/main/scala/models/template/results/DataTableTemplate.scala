package models.template.results

import java.util.UUID

import models.query.QueryResult
import models.template.Icons
import util.Messages

import scalatags.Text.all._

object DataTableTemplate {
  private[this] def tableHeader(res: QueryResult, containsRowNum: Boolean) = {
    def str(name: String) = if (res.source.exists(_.sortable)) {
      if (res.source.flatMap(_.sortedColumn).contains(name)) {
        val asc = !res.source.flatMap(_.sortedAscending).contains(false)
        span(cls := "sorted-title", data("col") := name, data("dir") := { if (asc) { "asc" } else { "desc" } })(
          i(cls := s"right sorted-icon fa ${if (asc) { Icons.sortedAsc } else { Icons.sortedDesc }}"),
          span(name)
        )
      } else {
        span(cls := "sorted-title", data("col") := name, data("dir") := "none")(
          i(cls := s"right sort-icon fa ${Icons.sortable}"),
          span(name)
        )
      }
    } else {
      span(cls := "unsorted-title")(name)
    }

    val ths = res.columns.map(c => th(data("t") := c.t.toString)(str(c.name))) :+ th(cls := "actions-column")("")
    if (containsRowNum) {
      thead(tr(ths))
    } else {
      thead(tr(th(cls := "row-num-col")("#") +: ths))
    }
  }

  def tableRows(res: QueryResult, key: String, resultId: UUID, containsRowNum: Boolean) = {
    val offset = res.source.map(_.dataOffset + 1).getOrElse(1)
    val actions = td(a(cls := "view-row-link theme-text", href := "")(i(cls := s"fa ${Icons.show}")))
    if (containsRowNum) {
      res.data.map(r => tr(cls := s"result-$resultId $key")(
        res.columns.zip(r).map(x => DataCellTemplate.cellValue(x._1, x._2)) :+ actions
      ))
    } else {
      res.data.zipWithIndex.map(r => tr(cls := s"result-$resultId $key")(
        td(cls := "row-num-col")(em((r._2 + offset).toString)) +: res.columns.zip(r._1).map(x => DataCellTemplate.cellValue(x._1, x._2)) :+ actions
      ))
    }
  }

  def forResults(res: QueryResult, key: String, resultId: UUID) = if (res.columns.isEmpty || res.data.isEmpty) {
    em(Messages("query.no.rows.returned"))
  } else {
    val containsRowNum = res.columns.headOption.exists(_.name == "#")
    div(cls := "query-result-table")(
      table(cls := "bordered highlight responsive-table")(
        tableHeader(res, containsRowNum),
        tbody(tableRows(res, key, resultId, containsRowNum))
      )
    )
  }
}
