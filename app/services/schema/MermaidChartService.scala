package services.schema

import models.schema.Schema

object MermaidChartService {
  private[this] val tableStart = "<table><thead><tr><th>Name</th><th>PK</th><th>NN</th><th>Type</th></tr></thead><tbody>"
  private[this] val tableEnd = "</tbody></table>"

  def chartFor(s: Schema) = divChartFor(s)

  def tableChartFor(s: Schema) = {
    val ret = new StringBuilder("graph TD\n")
    def addLine(l: String) = ret.append("  " + l + "\n")
    s.tables.foreach { t =>
      val cols = t.columns.map(c => s"""<tr><td>${c.name}</td><td>${if (c.primaryKey) { "fa:fa-check" } else { "" }}</td><td>${if (c.notNull) { "fa:fa-check" } else { "" }}</td><td>${c.columnType}</td></tr>""").mkString
      addLine(s"""${t.name}["<h6>fa:fa-folder-o ${t.name}</h6>$tableStart$cols$tableEnd"]""")
    }
    s.tables.foreach { t =>
      t.foreignKeys.foreach { fk =>
        addLine(s"""${t.name} --> ${fk.targetTable}""")
      }
    }
    ret.toString
  }

  def divChartFor(s: Schema) = {
    val ret = new StringBuilder("graph TD\n")
    def addLine(l: String) = ret.append("  " + l + "\n")
    s.tables.foreach { t =>

      val cols = t.columns.map(c => s"""<div><span>${c.columnType}${if (c.notNull) { "" } else { "*" }}</span>${c.name}</div>""").mkString
      addLine(s"""${t.name}["<h6>fa:fa-folder-o ${t.name}</h6>$cols"]""")
    }
    s.tables.foreach { t =>
      t.foreignKeys.foreach { fk =>
        addLine(s"""${t.name} --> ${fk.targetTable}""")
      }
    }
    ret.toString
  }
}
