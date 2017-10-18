package services.scalaexport.file

import models.scalaexport.ScalaFile
import services.scalaexport.config.ExportModel

object ServiceMutations {
  private[this] val trace = "(implicit trace: TraceData)"

  def mutations(model: ExportModel, file: ScalaFile) = {
    if (model.pkFields.nonEmpty) {
      model.pkFields.foreach(f => f.t.requiredImport.foreach(x => file.addImport(x, f.t.asScala)))
      val sig = model.pkFields.map(f => f.propertyName + ": " + f.t.asScala).mkString(", ")
      val audit = model.pkFields.map(f => f.propertyName + ".toString").mkString(", ")
      val call = model.pkFields.map(_.propertyName).mkString(", ")
      val interp = model.pkFields.map(f => "$" + f.propertyName).mkString(", ")
      file.add()
      file.add(s"def remove($sig)$trace = {", 1)
      file.add(s"""traceB("remove")(td => ApplicationDatabase.query(${model.className}Queries.getByPrimaryKey($call))(td) match {""", 1)
      file.add(s"case Some(current) =>", 1)
      file.add(s"""services.audit.AuditService.onRemove("${model.className}", Seq($audit), current.toDataFields)""")
      file.add(s"ApplicationDatabase.execute(${model.className}Queries.removeByPrimaryKey($call))(td)")
      file.add("current")
      file.indent(-1)
      file.add(s"""case None => throw new IllegalStateException(s"Cannot find ${model.className} matching [$interp].")""")
      file.add("})", -1)
      file.add("}", -1)
      file.add()
      file.add(s"def update($sig, fields: Seq[DataField])$trace = {", 1)
      file.add(s"""traceB("update")(td => ApplicationDatabase.query(${model.className}Queries.getByPrimaryKey($call))(td) match {""", 1)
      file.add(s"case Some(current) =>", 1)
      file.add(s"ApplicationDatabase.execute(${model.className}Queries.update($call, fields))(td)")
      file.add(s"ApplicationDatabase.query(${model.className}Queries.getByPrimaryKey($call))(td) match {", 1)
      file.add("case Some(newModel) =>", 1)
      val ids = model.pkFields.map {
        case f if f.notNull => s"""DataField("${f.propertyName}", Some(${f.propertyName}.toString))"""
        case f => s"""DataField("${f.propertyName}", ${f.propertyName}.map(_.toString))"""
      }.mkString(", ")
      file.add(s"""services.audit.AuditService.onUpdate("${model.className}", Seq($ids), current.toDataFields, fields)""")
      file.add("newModel")
      file.indent(-1)
      file.add(s"""case None => throw new IllegalStateException(s"Cannot find ${model.className} matching [$interp].")""")
      file.add("}", -1)
      file.add(s"""case None => throw new IllegalStateException(s"Cannot find ${model.className} matching [$interp].")""")
      file.add("})", -1)
      file.add("}", -1)
    }
  }
}
