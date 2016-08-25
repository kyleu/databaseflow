package models.template

import models.query.{SavedQuery, SharedResult}
import models.schema.{Procedure, Table, View}
import utils.TemplateUtils

import scalatags.Text.all._

object SidenavTemplate {
  private[this] def tagFor(tagId: String, key: String, link: String, name: String, icon: String) = li(a(
    id := tagId, cls := "sidenav-link waves-effect waves-light", data("key") := key, href := link, title := name, data("name") := name
  )(em(cls := s"fa $icon theme-text"), span(name)))

  private[this] def sharedResult(sr: SharedResult) = tagFor(
    "shared-result-link-" + sr.id, sr.id.toString, "#shared-result-" + sr.id, sr.title, Icons.sharedResult
  )
  def sharedResults(srs: Seq[SharedResult]) = srs.map(sharedResult)

  private[this] def savedQuery(sq: SavedQuery) = tagFor("saved-query-link-" + sq.id, sq.id.toString, "#saved-query-" + sq.id, sq.name, Icons.savedQuery)
  def savedQueries(sqs: Seq[SavedQuery]) = sqs.map(savedQuery)

  private[this] def table(t: Table) = tagFor("table-link-" + TemplateUtils.cleanForId(t.name), t.name, "#table-" + t.name, t.name, Icons.tableClosed)
  def tables(tables: Seq[Table]) = tables.map(table)

  private[this] def view(v: View) = tagFor("view-link-" + TemplateUtils.cleanForId(v.name), v.name, "#view-" + v.name, v.name, Icons.view)
  def views(views: Seq[View]) = views.map(view)

  private[this] def procedure(p: Procedure) = tagFor("procedure-link-" + TemplateUtils.cleanForId(p.name), p.name, "#view-" + p.name, p.name, Icons.procedure)
  def procedures(procedures: Seq[Procedure]) = procedures.map(procedure)
}
