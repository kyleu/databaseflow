package services.scalaexport.file

import models.scalaexport.ScalaFile
import models.schema.ColumnType
import services.scalaexport.config.ExportModel
import services.scalaexport.inject.InjectSearchParams

object ServiceHelper {
  private[this] val trace = "(implicit trace: TraceData)"

  def mutations(model: ExportModel, file: ScalaFile) = {
    if (model.pkFields.nonEmpty) {
      model.pkFields.foreach(f => f.t.requiredImport.foreach(x => file.addImport(x, f.t.asScala)))
      val sig = model.pkFields.map(f => f.propertyName + ": " + f.t.asScala).mkString(", ")
      val call = model.pkFields.map(_.propertyName).mkString(", ")
      val interp = model.pkFields.map(f => "$" + f.propertyName).mkString(", ")
      file.add(s"def remove($sig)$trace = {", 1)
      file.add(s"""traceF("remove")(td => MasterDatabase.query(${model.className}Queries.getByPrimaryKey($call))(td).flatMap {""", 1)
      file.add(s"case Some(current) => MasterDatabase.execute(${model.className}Queries.removeByPrimaryKey($call))(td).map(_ => current)")
      file.add(s"""case None => throw new IllegalStateException(s"Cannot find Note matching [$interp].")""")
      file.add("})", -1)
      file.add("}", -1)
      file.add()
      file.add(s"def update($sig, fields: Seq[DataField])$trace = {", 1)
      file.add(s"""traceF("update")(td => MasterDatabase.query(${model.className}Queries.getByPrimaryKey($call))(td).flatMap {""", 1)
      file.add(s"case Some(current) => MasterDatabase.execute(${model.className}Queries.update($call, fields))(td).flatMap { _ =>", 1)
      file.add(s"MasterDatabase.query(${model.className}Queries.getByPrimaryKey($call))(td).map {", 1)
      file.add("case Some(newModel) => newModel")
      file.add(s"""case None => throw new IllegalStateException(s"Cannot find ${model.className} matching [$interp].")""")
      file.add("}", -1)
      file.add("}", -1)
      file.add(s"""case None => throw new IllegalStateException(s"Cannot find ${model.className} matching [$interp].")""")
      file.add("})", -1)
      file.add("}", -1)
    }
  }

  private[this] val td = "(implicit trace: TraceData)"

  def addGetters(model: ExportModel, file: ScalaFile) = {
    model.pkFields.foreach(field => field.t.requiredImport.foreach(pkg => file.addImport(pkg, field.t.asScala)))
    model.pkFields match {
      case Nil => // noop
      case field :: Nil =>
        val colProp = field.propertyName
        file.add(s"def getByPrimaryKey($colProp: ${field.t.asScala})$td = {", 1)
        file.add(s"""traceF("get.by.primary.key")(td => MasterDatabase.query(${model.className}Queries.getByPrimaryKey($colProp))(td))""")
        file.add("}", -1)
        val seqArgs = s"${colProp}Seq: Seq[${field.t.asScala}]"
        file.add(s"def getByPrimaryKeySeq($seqArgs)$td = {", 1)
        file.add(s"""traceF("get.by.primary.key.seq")(td => MasterDatabase.query(${model.className}Queries.getByPrimaryKeySeq(${colProp}Seq))(td))""")
        file.add("}", -1)
        field.t match {
          case ColumnType.UuidType => file.addMarker("uuid-search", InjectSearchParams(model).toString)
          case ColumnType.IntegerType => file.addMarker("int-search", InjectSearchParams(model).toString)
          case _ => // noop
        }
      case fields => // multiple columns
        val tupleTyp = "(" + fields.map(_.t.asScala).mkString(", ") + ")"
        val colArgs = fields.map(f => f.propertyName + ": " + f.t.asScala).mkString(", ")
        val queryArgs = fields.map(_.propertyName).mkString(", ")

        file.add(s"def getByPrimaryKey($colArgs)$td = {", 1)
        file.add(s"""traceF("get.by.primary.key")(td => MasterDatabase.query(${model.className}Queries.getByPrimaryKey($queryArgs))(td))""")
        file.add("}", -1)

        file.add(s"def getByPrimaryKeySeq(pkSeq: Seq[$tupleTyp])$td = {", 1)
        file.add(s"""traceF("get.by.primary.key.seq")(td => MasterDatabase.query(${model.className}Queries.getByPrimaryKeySeq(pkSeq))(td))""")
        file.add("}", -1)
    }
    file.add()
  }
}
