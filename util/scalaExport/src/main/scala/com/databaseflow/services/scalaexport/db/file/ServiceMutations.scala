package com.databaseflow.services.scalaexport.db.file

import com.databaseflow.models.scalaexport.db.ExportModel
import com.databaseflow.models.scalaexport.file.ScalaFile

object ServiceMutations {
  private[this] val trace = "(implicit trace: TraceData)"

  def mutations(rootPrefix: String, model: ExportModel, file: ScalaFile) = {
    if (model.pkFields.nonEmpty) {
      model.pkFields.foreach(_.addImport(file))
      val sig = model.pkFields.map(f => f.propertyName + ": " + f.scalaType).mkString(", ")
      val call = model.pkFields.map(_.propertyName).mkString(", ")
      val interp = model.pkFields.map("$" + _.propertyName).mkString(", ")
      file.addImport("scala.concurrent", "Future")
      file.add()
      file.add(s"def remove(creds: Credentials, $sig)$trace = {", 1)
      file.add(s"""traceF("remove")(td => getByPrimaryKey(creds, $call)(td).flatMap {""", 1)
      file.add(s"case Some(current) =>", 1)
      if (model.audited) {
        val audit = model.pkFields.map(f => f.propertyName + ".toString").mkString(", ")
        file.add(s"""${rootPrefix}services.audit.AuditHelper.onRemove("${model.className}", Seq($audit), current.toDataFields, creds)""")
      }
      file.add(s"ApplicationDatabase.executeF(${model.className}Queries.removeByPrimaryKey($call))(td).map(_ => current)")
      file.indent(-1)
      file.add(s"""case None => throw new IllegalStateException(s"Cannot find ${model.className} matching [$interp].")""")
      file.add("})", -1)
      file.add("}", -1)
      file.add()

      file.add(s"def update(creds: Credentials, $sig, fields: Seq[DataField])$trace = {", 1)
      file.add(s"""traceF("update")(td => getByPrimaryKey(creds, $call)(td).flatMap {""", 1)
      file.add(s"""case Some(current) if fields.isEmpty => Future.successful(current -> s"No changes required for ${model.title} [$interp].")""")
      file.add(s"case Some(current) => ApplicationDatabase.executeF(${model.className}Queries.update($call, fields))(td).flatMap { _ =>", 1)
      file.add(s"getByPrimaryKey(creds, $call)(td).map {", 1)
      file.add("case Some(newModel) =>", 1)
      val ids = model.pkFields.map {
        case f if f.notNull => s"""DataField("${f.propertyName}", Some(${f.propertyName}.toString))"""
        case f => s"""DataField("${f.propertyName}", ${f.propertyName}.map(_.toString))"""
      }.mkString(", ")
      if (model.audited) {
        file.add(s"""${rootPrefix}services.audit.AuditHelper.onUpdate("${model.className}", Seq($ids), newModel.toDataFields, fields, creds)""")
      }
      file.add(s"""newModel -> s"Updated [$${fields.size}] fields of ${model.title} [$interp]."""")
      file.indent(-1)
      file.add(s"""case None => throw new IllegalStateException(s"Cannot find ${model.className} matching [$interp].")""")
      file.add("}", -1)
      file.add("}", -1)
      file.add(s"""case None => throw new IllegalStateException(s"Cannot find ${model.className} matching [$interp].")""")
      file.add("})", -1)
      file.add("}", -1)
    }
  }
}
