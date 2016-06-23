package models.template

import models.audit.AuditRecord

import scalatags.Text.all._

object HistoryTemplate {
  val panel = {
    val content = div(id := "history-panel")(
      div(cls := "history-content")("Loading query history...")
    )

    StaticPanelTemplate.cardRow(
      content = content,
      iconAndTitle = Some(Icons.history -> "Query History"),
      actions = Seq(
        div(a(cls := "refresh-history-link", href := "")("Refresh"))
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
            tr(th("Id"), th("Type"), th("Status"), th("Context"), th("SQL"), th("Occurred"), th("Actions")),
            tbody(h.map { history =>
              tr(id := s"history-${history.id}")(
                td(history.id.toString),
                td(history.auditType.toString),
                td(history.status.toString),
                td(history.context),
                td(pre(cls := "sql-pre")(history.sql)),
                td(history.occurred),
                td(a(data("audit") := history.id.toString, href := "#", cls := "audit-remove")("Remove"))
              )
            })
          )
        )
      )
    }
  }
}
