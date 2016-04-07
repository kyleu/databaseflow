package models.template

import models.query.SavedQuery
import models.schema.{ Procedure, Table }

import scalatags.Text.all._

object SidenavTemplate {
  private[this] def savedQuery(sq: SavedQuery) = li(
    a(id := "sidenav-saved-query-" + sq.id, cls := "sidenav-link waves-effect waves-light", title := sq.name, href := "#saved-query-" + sq.id)(
      em(cls := s"fa ${Icons.savedQuery}"),
      span(title := sq.name)(sq.name)
    )
  )
  def savedQueries(sqs: Seq[SavedQuery]) = sqs.map(savedQuery)

  private[this] def table(t: Table) = li(a(id := "sidenav-table-" + t.name, cls := "sidenav-link waves-effect waves-light", href := "#table-" + t)(
    em(cls := s"fa ${Icons.tableClosed}"),
    span(title := t.name)(t.name)
  ))
  def tables(tables: Seq[Table]) = tables.map(table)

  private[this] def view(v: Table) = li(a(id := "sidenav-view-" + v.name, cls := "sidenav-link waves-effect waves-light", href := "#view-" + v)(
    em(cls := s"fa ${Icons.view}"),
    span(title := v.name)(v.name)
  ))
  def views(views: Seq[Table]) = views.map(view)

  private[this] def procedure(p: Procedure) = li(
    a(id := "sidenav-procedure-" + p.name, cls := "sidenav-link waves-effect waves-light", href := "#procedure-" + p)(
      em(cls := s"fa ${Icons.procedure}"),
      span(title := p.name)(p.name)
    )
  )
  def procedures(procedures: Seq[Procedure]) = procedures.map(procedure)
}
