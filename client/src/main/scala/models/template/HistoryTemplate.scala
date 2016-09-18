package models.template

import models.audit.AuditRecord
import utils.{Messages, TemplateUtils}

import scalatags.Text.all._

object HistoryTemplate {
  val panel = {
    val content = div(id := "history-panel")(
      div(cls := "history-content z-depth-1")(Messages("general.loading"))
    )

    StaticPanelTemplate.panelRow(
      content = content,
      iconAndTitle = Some(Icons.history -> span(Messages("history.title"))),
      actions = Seq(
        div(a(cls := "right-link remove-all-history-link theme-text", href := "")(Messages("history.remove.all"))),
        div(a(cls := "refresh-history-link theme-text", href := "")(Messages("general.refresh")))
      )
    )
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
                td(TemplateUtils.toTimeago(TemplateUtils.toIsoString(history.occurred))),
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
