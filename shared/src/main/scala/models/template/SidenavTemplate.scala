package models.template

import models.query.SavedQuery

import scalatags.Text.all._

object SidenavTemplate {
  private[this] def savedQuery(sq: SavedQuery) = li(
    a(id := "saved-query-" + sq.id, cls := "saved-query-link waves-effect waves-light", title := sq.name, href := "#saved-query-" + sq.id)(
      em(cls := s"fa ${Icons.savedQuery}"),
      span(title := sq.name)(sq.name)
    )
  )
  def savedQueries(sqs: Seq[SavedQuery]) = sqs.map(savedQuery)

  private[this] def table(t: String) = li(a(id := "table-" + t, cls := "table-link waves-effect waves-light", href := "#table-" + t)(
    em(cls := s"fa ${Icons.tableClosed}"),
    span(title := t)(t)
  ))
  def tables(tables: Seq[String]) = tables.map(table)

  private[this] def view(v: String) = li(a(id := "view-" + v, cls := "view-link waves-effect waves-light", href := "#view-" + v)(
    em(cls := s"fa ${Icons.view}"),
    span(title := v)(v)
  ))
  def views(views: Seq[String]) = views.map(view)

  private[this] def procedure(p: String) = li(a(id := "procedure-" + p, cls := "procedure-link waves-effect waves-light", href := "#procedure-" + p)(
    em(cls := s"fa ${Icons.procedure}"),
    span(title := p)(p)
  ))
  def procedures(procedures: Seq[String]) = procedures.map(procedure)
}
