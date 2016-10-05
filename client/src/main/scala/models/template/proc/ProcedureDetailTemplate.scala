package models.template.proc

import java.util.UUID

import models.schema.ProcedureParam
import models.template.{Icons, StaticPanelTemplate}
import utils.Messages

import scalatags.Text.TypedTag
import scalatags.Text.all._

object ProcedureDetailTemplate {
  def forProcedure(queryId: UUID, procedureName: String) = {
    val content = div(
      div(cls := "description")(""),
      ul(cls := "collapsible table-options", data("collapsible") := "expandable")(
        li(cls := "definition-section initially-hidden")(
          div(cls := "collapsible-header")(i(cls := s"fa ${Icons.definition}"), Messages("th.definition")),
          div(cls := "collapsible-body")(div(cls := "section-content")(Messages("general.loading")))
        ),
        li(cls := "params-section initially-hidden")(
          div(cls := "collapsible-header")(i(cls := s"fa ${Icons.columns}"), Messages("th.parameters"), span(cls := "badge")("")),
          div(cls := "collapsible-body")(div(cls := "section-content")(Messages("general.loading")))
        )
      )
    )

    val actions = Seq.empty[TypedTag[String]]
    //(a(cls := "call-procedure-link theme-text right", href := "#")(Messages("query.call")),
    //div(cls := "clear")

    div(id := s"panel-$queryId", cls := "workspace-panel")(
      StaticPanelTemplate.panelRow(content, iconAndTitle = Some(Icons.procedure -> span(procedureName)), actions = actions),
      div(id := s"workspace-$queryId")
    )
  }

  def paramsPanel(params: Seq[ProcedureParam]) = table(cls := "bordered highlight responsive-table")(
    thead(tr(
      th(Messages("th.name")),
      th(Messages("th.description")),
      th(Messages("th.type")),
      th(Messages("th.column"))
    )),
    tbody(params.map { p =>
      tr(
        td(p.name),
        td(p.description.getOrElse(""): String),
        td(p.paramType),
        td(p.columnType.toString)
      )
    })
  )
}
