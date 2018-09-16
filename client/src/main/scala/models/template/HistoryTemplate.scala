package models.template

import models.audit.AuditRecord
import util.{Messages, TemplateHelper}

import scalatags.Text.all._

object HistoryTemplate {
  val panel = {
    val content = div(id := "history-panel")(
      div(cls := "history-content")(Messages("general.loading"))
    )

    StaticPanelTemplate.row(StaticPanelTemplate.panel(
      content = content,
      iconAndTitle = Some(Icons.history -> span(Messages("history.title"))),
      actions = Seq(
        div(a(cls := "right-link remove-all-history-link theme-text", href := "")(Messages("history.remove.all"))),
        div(a(cls := "refresh-history-link theme-text", href := "")(Messages("general.refresh")))
      )
    ))
  }

  def content(h: Seq[AuditRecord]) = {
    if (h.isEmpty) {
      p(Messages("history.no.records"))
    } else {
      div(
        table(cls := "history-table")(
          thead(
            tr(th(Messages("th.status")), th(Messages("th.sql")), th(Messages("th.occurred")), th("")),
            tbody(h.map { history =>
              tr(id := s"history-${history.id}")(
                td(history.status.toString),
                td(pre(cls := "sql-pre")(history.sql)),
                td(TemplateHelper.toTimeago(TemplateHelper.toIsoString(history.occurred))),
                td(a(data("audit") := history.id.toString, href := "#", cls := "audit-remove theme-text", title := Messages("general.close"))(
                  i(cls := "fa " + Icons.close)
                ))
              )
            })
          )
        )
      )
    }
  }
}
