package models.template

import models.audit.AuditRecord
import utils.TemplateUtils

import scalatags.Text.all._

object HistoryTemplate {
  val panel = {
    val content = div(id := "history-panel")(
      div(cls := "history-content")("Loading query history...")
    )

    StaticPanelTemplate.cardRow(
      content = content,
      iconAndTitle = Some(Icons.history -> span("Query History")),
      actions = Seq(
        div(a(cls := "right-link remove-all-history-link theme-text", href := "")("Remove All History")),
        div(a(cls := "refresh-history-link theme-text", href := "")("Refresh"))
      )
    )
  }

  def content(h: Seq[AuditRecord]) = {
    if (h.isEmpty) {
      p("It looks like you haven't run any queries for this database. Get started!")
    } else {
      div(
        table(cls := "history-table")(
          thead(
            tr(th("Status"), th("SQL"), th("Occurred"), th("")),
            tbody(h.map { history =>
              tr(id := s"history-${history.id}")(
                td(history.status.toString),
                td(pre(cls := "sql-pre")(history.sql)),
                td(TemplateUtils.toTimeago(TemplateUtils.toIsoString(history.occurred))),
                td(a(data("audit") := history.id.toString, href := "#", cls := "audit-remove theme-text")(i(cls := "fa " + Icons.close)))
              )
            })
          )
        )
      )
    }
  }
}
