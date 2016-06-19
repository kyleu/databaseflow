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
      p(s"Wow, ${h.length} queries! Good job!")
    }
  }
}
