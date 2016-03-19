package models.templates

import models.schema.Schema

import scalatags.Text.all._

object SidenavTemplate {
  def tables(schema: Schema) = {
    schema.tables.map { t =>
      li(a(href := "")(t.name))
    }
  }

  def procedures(schema: Schema) = {
    schema.procedures.map { t =>
      li(a(href := "")(t.name))
    }
  }

  def views(schema: Schema) = {
    schema.views.map { t =>
      li(a(href := "")(t.name))
    }
  }
}
