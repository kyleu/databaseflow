package models.template

import java.util.UUID

import models.query.{SavedQuery, SharedResult}
import models.schema.{EnumType, Procedure, Table, View}
import util.{Messages, NumberUtils}

import scalatags.Text.TypedTag
import scalatags.Text.all._

object ModelListTemplate {
  private[this] def messagesHeaders(keys: String*) = keys.map(s => Messages("th." + s))

  def forSavedQueries(queryId: UUID, savedQueries: Seq[SavedQuery], usernameMap: Map[UUID, String], selfId: UUID) = {
    val cols = messagesHeaders("name", "owner", "read", "edit", "connection")
    val rows = savedQueries.map(sq => tr(
      td(a(cls := "list-link theme-text", data("name") := sq.id.toString, href := s"#saved-query-${sq.id}")(sq.name)),
      td(usernameMap.get(sq.owner) match {
        case Some(username) => span(username)
        case None => em(Messages("th.unknown"))
      }),
      td(sq.read.toString),
      td(sq.edit.toString),
      td(sq.connection match {
        case Some(o) => if (selfId == o) {
          span(Messages("list.this.connection"))
        } else {
          em(Messages("list.unknown.connection"))
        }
        case None => em(Messages("list.all.connections"))
      })
    ))
    forModels(queryId, Messages("list.saved.queries"), tableFor(cols, rows))
  }

  def forSharedResults(queryId: UUID, sharedResults: Seq[SharedResult], usernameMap: Map[UUID, String]) = {
    val cols = messagesHeaders("title", "owner")
    val rows = sharedResults.map(sr => tr(
      td(a(cls := "list-link theme-text", data("name") := sr.id.toString, href := s"#shared-result-${sr.id}")(sr.title)),
      td(usernameMap.get(sr.owner) match {
        case Some(username) => span(username)
        case None => em(Messages("th.unknown"))
      })
    ))
    forModels(queryId, Messages("list.shared.results"), tableFor(cols, rows))
  }

  def forTables(queryId: UUID, tables: Seq[Table]) = {
    val cols = messagesHeaders("name", "rows", "columns", "primary.key", "indexes", "foreign.keys", "description")
    val rows = tables.map(t => tr(
      td(a(cls := "list-link theme-text", data("name") := t.name, href := s"#table-${t.name}")(t.name)),
      td(t.rowCountEstimate.map(NumberUtils.withCommas).getOrElse(""): String),
      td(title := t.columns.map(_.name).mkString(", "))(t.columns.size.toString),
      td(t.primaryKey.map(pk => span(pk.columns.mkString(", "))).getOrElse(em("None"))),
      td(title := t.indexes.map(_.name).mkString(", "))(t.indexes.size.toString),
      td(title := t.foreignKeys.map(_.name).mkString(", "))(t.foreignKeys.size.toString),
      td(t.description.map(d => span(d)).getOrElse(em("None")))
    ))
    forModels(queryId, Messages("list.tables"), tableFor(cols, rows))
  }

  def forViews(queryId: UUID, views: Seq[View]) = {
    val cols = messagesHeaders("name", "columns", "description")
    val rows = views.map(v => tr(
      td(a(cls := "list-link theme-text", data("name") := v.name, href := s"#view-${v.name}")(v.name)),
      td(v.columns.map(_.name).mkString(", ")),
      td(v.description.map(d => span(d)).getOrElse(em("None")))
    ))
    forModels(queryId, Messages("list.views"), tableFor(cols, rows))
  }

  def forProcedures(queryId: UUID, procedures: Seq[Procedure]) = {
    val cols = messagesHeaders("name", "parameters", "returns.result")
    val rows = procedures.map(p => tr(
      td(a(cls := "list-link theme-text", data("name") := p.name, href := s"#procedure-${p.name}")(p.name)),
      td(p.params.map(param => s"${param.name} (${param.paramType} ${param.columnType})").mkString(", ")),
      td(p.returnsResult.getOrElse(false).toString())
    ))
    forModels(queryId, Messages("list.procedures"), tableFor(cols, rows))
  }

  def forEnums(queryId: UUID, enums: Seq[EnumType]) = {
    val cols = messagesHeaders("name", "values")
    val rows = enums.map(e => tr(
      td(a(cls := "list-link theme-text", data("name") := e.key, href := s"#enum-${e.key}")(e.key)),
      td(e.values.mkString(", "))
    ))
    forModels(queryId, Messages("list.enums"), tableFor(cols, rows))
  }

  private[this] def tableFor(cols: Seq[String], rows: Seq[TypedTag[String]]) = table(cls := "bordered highlight responsive-table")(
    thead(tr(cols.map(c => th(c)))),
    tbody(rows: _*)
  )

  private[this] def forModels(queryId: UUID, name: String, t: TypedTag[String]) = {
    val searchBox = input(cls := "model-filter", placeholder := Messages("list.filter", name), `type` := "text")
    val ret = div(
      StaticPanelTemplate.row(StaticPanelTemplate.panel(div(searchBox, t), Some(Icons.list -> span(name)))),
      div(id := s"workspace-$queryId")
    )
    name -> ret
  }
}
