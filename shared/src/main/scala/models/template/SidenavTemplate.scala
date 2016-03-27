package models.template

import models.query.SavedQuery

import scalatags.Text.all._

object SidenavTemplate {
  def savedQueries(sqs: Seq[SavedQuery]) = {
    sqs.map { sq =>
      li(a(id := "saved-query-" + sq.id, cls := "saved-query-link waves-effect waves-light", title := sq.title, href := "#saved-query-" + sq.id)(
        em(cls := s"fa ${Icons.savedQuery}"),
        span(sq.title)
      ))
    }
  }

  def tables(tables: Seq[String]) = {
    tables.map { t =>
      li(a(id := "table-" + t, cls := "table-link waves-effect waves-light", href := "#table-" + t)(
        em(cls := s"fa ${Icons.table}"),
        span(t)
      ))
    }
  }

  def views(views: Seq[String]) = {
    views.map { v =>
      li(a(id := "view-" + v, cls := "view-link waves-effect waves-light", href := "#view-" + v)(
        em(cls := s"fa ${Icons.view}"),
        span(v)
      ))
    }
  }

  def procedures(procedures: Seq[String]) = {
    procedures.map { p =>
      li(a(id := "procedure-" + p, cls := "procedure-link waves-effect waves-light", href := "#procedure-" + p)(
        em(cls := s"fa ${Icons.procedure}"),
        span(p)
      ))
    }
  }
}
