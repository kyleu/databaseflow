package services.scalaexport.db.file

import models.scalaexport.db.ExportModel
import models.scalaexport.file.ScalaFile

object ServiceInserts {
  def insertsFor(rootPrefix: String, model: ExportModel, queriesFilename: String, file: ScalaFile) = {
    file.add("// Mutations")
    file.add(s"""def insert(creds: Credentials, model: ${model.className})(implicit trace: TraceData) = traceF("insert") { td =>""", 1)
    file.add(s"""ApplicationDatabase.executeF($queriesFilename.insert(model))(td).flatMap {""", 1)
    if (model.pkFields.isEmpty) {
      file.add(s"case _ => scala.concurrent.Future.successful(None: Option[${model.className}])")
    } else {
      if (model.audited) {
        file.add(s"case 1 => getByPrimaryKey(creds, ${model.pkFields.map(f => "model." + f.propertyName).mkString(", ")})(td).map {", 1)
        val audit = model.pkFields.map(f => "n." + f.propertyName + ".toString").mkString(", ")
        file.add("case Some(n) =>")
        file.add(s"""  ${rootPrefix}services.audit.AuditHelper.onInsert("${model.className}", Seq($audit), n.toDataFields, creds)""")
        file.add("  model")
        file.add(s"""case None => throw new IllegalStateException("Unable to find ${model.title}.")""")
        file.add("}", -1)
      } else {
        file.add(s"case 1 => getByPrimaryKey(creds, ${model.pkFields.map(f => "model." + f.propertyName).mkString(", ")})(td)")
      }
      file.add(s"""case _ => throw new IllegalStateException("Unable to find newly-inserted ${model.title}.")""")
    }
    file.add("}", -1)
    file.add("}", -1)

    file.add(s"def insertBatch(creds: Credentials, models: Seq[${model.className}])(implicit trace: TraceData) = {", 1)
    file.add(s"""traceF("insertBatch")(td => ApplicationDatabase.executeF($queriesFilename.insertBatch(models))(td))""")
    file.add("}", -1)

    file.add(s"""def create(creds: Credentials, fields: Seq[DataField])(implicit trace: TraceData) = traceF("create") { td =>""", 1)
    file.add(s"""ApplicationDatabase.executeF($queriesFilename.create(fields))(td).flatMap { _ =>""", 1)
    model.pkFields match {
      case Nil => file.add(s"Future.successful(None: Option[${model.className}])")
      case pk =>
        val lookup = pk.map(k => k.fromString(s"""fieldVal(fields, "${k.propertyName}")""")).mkString(", ")
        if (model.audited) {
          val audit = pk.map(k => s"""fieldVal(fields, "${k.propertyName}")""").mkString(", ")
          file.add(s"""${rootPrefix}services.audit.AuditHelper.onInsert("${model.className}", Seq($audit), fields, creds)""")
        }
        file.add(s"getByPrimaryKey(creds, $lookup)")
    }
    file.add("}", -1)
    file.add("}", -1)
  }
}
