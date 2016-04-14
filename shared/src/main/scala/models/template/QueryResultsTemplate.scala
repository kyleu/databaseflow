package models.template

import models.QueryResultResponse

import scalatags.Text.all._
import scalatags.Text.tags2.time

object QueryResultsTemplate {
  def forResults(qr: QueryResultResponse, dateIsoString: String, dateFullString: String) = {
    val res = qr.result
    val cardTitle = res.title

    val content = div(
      p(s"${qr.result.data.size} rows returned ", time(cls := "timeago", "datetime".attr := dateIsoString)(dateFullString), s" in [${qr.durationMs}ms]."),
      DataTableTemplate.forResults(res),
      div(cls := "z-depth-1 query-result-sql")(
        pre(cls := "pre-wrap")(res.sql)
      )
    )

    div(id := qr.id.toString)(
      StaticPanelTemplate.cardRow(
        title = cardTitle,
        content = content,
        icon = Some(Icons.queryResults),
        actions = Some(Seq(
          a(cls := "right results-sql-link", href := "#")("Show SQL"),
          a(cls := "results-download-link", href := "#")("Download")
        ))
      )
    )
  }
}
