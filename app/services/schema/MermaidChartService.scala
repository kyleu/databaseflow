package services.schema

import models.schema.Schema

object MermaidChartService {
  def chartFor(s: Schema) = {
    val ret = new StringBuilder("graph TD\n")
    def addLine(l: String) = ret.append("  " + l + "\n")
    s.tables.foreach { t =>
      val cols = t.columns.map(c => s"""<div><span>${c.columnType}</span>${c.name}</div>""").mkString
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
