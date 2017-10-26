package services.scalaexport.file

import models.scalaexport.ScalaFile
import services.scalaexport.config.ExportModel

object ServiceMutations {
  private[this] val trace = "(implicit trace: TraceData)"

  def mutations(model: ExportModel, file: ScalaFile) = {
    if (model.pkFields.nonEmpty) {
      model.pkFields.foreach(f => f.t.requiredImport.foreach(x => file.addImport(x, f.t.asScala)))
      val sig = model.pkFields.map(f => f.propertyName + ": " + f.t.asScala).mkString(", ")
      val call = model.pkFields.map(_.propertyName).mkString(", ")
      val interp = model.pkFields.map("$" + _.propertyName).mkString(", ")
      file.add()
      file.add(s"def remove(creds: Credentials, $sig, doAudit: Boolean = true)$trace = {", 1)
      file.add(s"""traceB("remove")(td => getByPrimaryKey(creds, $call)(td) match {""", 1)
      file.add(s"case Some(current) =>", 1)
      val audit = model.pkFields.map(f => f.propertyName + ".toString").mkString(", ")
      file.add(s"""if (doAudit) { services.audit.AuditHelper.onRemove("${model.className}", Seq($audit), current.toDataFields, creds) }""")
      file.add(s"ApplicationDatabase.execute(${model.className}Queries.removeByPrimaryKey($call))(td)")
      file.add("current")
      file.indent(-1)
      file.add(s"""case None => throw new IllegalStateException(s"Cannot find ${model.className} matching [$interp].")""")
      file.add("})", -1)
      file.add("}", -1)
      file.add()
      file.add(s"def update(creds: Credentials, $sig, fields: Seq[DataField], doAudit: Boolean = true)$trace = {", 1)
      file.add(s"""traceB("update")(td => getByPrimaryKey(creds, $call)(td) match {""", 1)
      file.add(s"""case Some(current) if fields.isEmpty => current -> s"No changes required for ${model.title} [$interp]."""")
      file.add(s"case Some(current) =>", 1)
      file.add(s"ApplicationDatabase.execute(${model.className}Queries.update($call, fields))(td)")
      file.add(s"getByPrimaryKey(creds, $call)(td) match {", 1)
      file.add("case Some(newModel) =>", 1)
      val ids = model.pkFields.map {
        case f if f.notNull => s"""DataField("${f.propertyName}", Some(${f.propertyName}.toString))"""
        case f => s"""DataField("${f.propertyName}", ${f.propertyName}.map(_.toString))"""
      }.mkString(", ")
      file.add(s"""if (doAudit) { services.audit.AuditHelper.onUpdate("${model.className}", Seq($ids), current.toDataFields, fields, creds) }""")
      file.add(s"""newModel -> s"Updated [$${fields.size}] fields of ${model.title} [$interp]."""")
      file.indent(-1)
      file.add(s"""case None => throw new IllegalStateException(s"Cannot find ${model.className} matching [$interp].")""")
      file.add("}", -1)
      file.indent(-1)
      file.add(s"""case None => throw new IllegalStateException(s"Cannot find ${model.className} matching [$interp].")""")
      file.add("})", -1)
      file.add("}", -1)
    }
  }
}
