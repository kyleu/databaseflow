package services.scalaexport.file

import models.scalaexport.ScalaFile
import models.schema.ColumnType
import services.scalaexport.config.ExportModel
import services.scalaexport.inject.InjectSearchParams

object ServiceHelper {
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

  def writeForeignKeys(model: ExportModel, file: ScalaFile) = model.foreignKeys.foreach { fk =>
    val searchArgs = "orderBys: Seq[OrderBy], limit: Option[Int], offset: Option[Int]"
    fk.references.toList match {
      case h :: Nil =>
        val col = model.fields.find(_.columnName == h.source).getOrElse(throw new IllegalStateException(s"Missing column [${h.source}]."))
        col.t.requiredImport.foreach(pkg => file.addImport(pkg, col.t.asScala))
        val propId = col.propertyName
        val propCls = col.className

        file.add(s"""def countBy$propCls($propId: ${col.t.asScala})(implicit trace: TraceData) = traceF("count.by.$propId") { td =>""", 1)
        file.add(s"MasterDatabase.query(${model.className}Queries.CountBy$propCls($propId))(td)")
        file.add("}", -1)
        file.add(s"def getBy$propCls($propId: ${col.t.asScala}, $searchArgs)(implicit trace: TraceData) = {", 1)
        file.add(s"""traceF("get.by.$propId")(td => MasterDatabase.query(${model.className}Queries.GetBy$propCls($propId, orderBys, limit, offset))(td))""")
        file.add("}", -1)
        file.add(s"""def getBy${propCls}Seq(${propId}Seq: Seq[${col.t.asScala}])(implicit trace: TraceData) = traceF("get.by.$propId.seq") { td =>""", 1)
        file.add(s"MasterDatabase.query(${model.className}Queries.GetBy${propCls}Seq(${propId}Seq))(td)")
        file.add("}", -1)

        file.add()
      case _ => // noop
    }
  }
}
