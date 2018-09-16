package models.template

import models.query.{SavedQuery, SharedResult}
import models.schema.{EnumType, Procedure, Table, View}
import util.{StringKeyUtils, TemplateHelper}
import scalatags.Text.all._

object SidenavTemplate {
  private[this] def tagFor(tagId: String, key: String, link: String, name: String, icon: String) = li(a(
    id := tagId, cls := "sidenav-link waves-effect waves-light", data("key") := key, href := link, title := name, data("name") := name
  )(em(cls := s"fa $icon theme-text"), span(name)))

  private[this] def tag(t: String, id: String, name: String, icon: String) = tagFor(t + "-link-" + id, id, "#" + t + "-" + id, name, icon)

  private[this] def sharedResult(sr: SharedResult) = tag("shared-result", sr.id.toString, sr.title, Icons.sharedResult)
  def sharedResults(srs: Seq[SharedResult]) = srs.map(sharedResult)

  private[this] def savedQuery(sq: SavedQuery) = tag("saved-query", sq.id.toString, sq.name, Icons.savedQuery)
  def savedQueries(sqs: Seq[SavedQuery]) = sqs.map(savedQuery)

  private[this] def table(t: Table) = tag("table-link", StringKeyUtils.cleanName(t.name), t.name, Icons.tableClosed)
  def tables(tables: Seq[Table]) = tables.map(table)

  private[this] def view(v: View) = tag("view", StringKeyUtils.cleanName(v.name), v.name, Icons.view)
  def views(views: Seq[View]) = views.map(view)

  private[this] def procedure(p: Procedure) = tag("procedure", StringKeyUtils.cleanName(p.name), p.name, Icons.procedure)
  def procedures(procedures: Seq[Procedure]) = procedures.map(procedure)

  private[this] def enum(e: EnumType) = tag("enum", StringKeyUtils.cleanName(e.key), e.key, Icons.enum)
  def enums(enums: Seq[EnumType]) = enums.sortBy(_.key).map(enum)
}
