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
      p("It looks like you haven't run any queries. Get started!")
    } else {
      div(
        p(s"Wow, ${h.length} queries! Good job!"),
        table(cls := "")(
          thead(
            tr(th("Id"), th("Type"), th("Status"), th("Context"), th("SQL"), th("Occurred"), th("Actions")),
            tbody(h.map { history =>
              tr(
                td(history.id.toString),
                td(history.auditType.toString),
                td(history.status.toString),
                td(history.context),
                td(history.sql),
                td(history.occurred),
                td(a(data("audit-id") := history.id.toString, href := "#", cls := "audit-remove")("Remove"))
              )
            })
          )
        )
      )
    }
  }
}
