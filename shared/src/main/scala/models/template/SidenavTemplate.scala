package models.template

import models.schema.Schema

import scalatags.Text.all._

object SidenavTemplate {
  def tables(schema: Schema) = {
    schema.tables.map { t =>
      li(a(title := t.description.getOrElse(""), href := "")(t.name))
    }
  }

  def procedures(schema: Schema) = {
    schema.procedures.map { p =>
      li(a(title := p.description.getOrElse(""), href := "")(p.name))
    }
  }

  def views(schema: Schema) = {
    schema.views.map { v =>
      li(a(title := v.description.getOrElse(""), href := "")(v.name))
    }
  }
}
