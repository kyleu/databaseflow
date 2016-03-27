package models.template

import models.query.SavedQuery
import models.schema.Schema

import scalatags.Text.all._

object SidenavTemplate {
  def savedQueries(sqs: Seq[SavedQuery]) = {
    sqs.map { sq =>
      li(a(id := "saved-query-" + sq.id, cls := "saved-query-link waves-effect waves-light", title := sq.title, href := "#saved-query-" + sq.id)(
        em(cls := "fa fa-envelope-o"),
        span(sq.title)
      ))
    }
  }

  def tables(schema: Schema) = {
    schema.tables.map { t =>
      li(a(id := "table-" + t.name, cls := "table-link waves-effect waves-light", title := t.description.getOrElse(""), href := "#table-" + t.name)(
        em(cls := "fa fa-folder-o"),
        span(t.name)
      ))
    }
  }

  def procedures(schema: Schema) = {
    schema.procedures.map { p =>
      li(a(id := "procedure-" + p.name, cls := "procedure-link waves-effect waves-light", title := p.description.getOrElse(""), href := "#procedure-" + p.name)(
        em(cls := "fa fa-code"),
        span(p.name)
      ))
    }
  }

  def views(schema: Schema) = {
    schema.views.map { v =>
      li(a(id := "view-" + v.name, cls := "view-link waves-effect waves-light", title := v.description.getOrElse(""), href := "#view-" + v.name)(
        em(cls := "fa fa-bar-chart"),
        span(v.name)
      ))
    }
  }
}
