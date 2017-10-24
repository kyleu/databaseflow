package services.scalaexport.file

import models.scalaexport.ScalaFile
import models.schema.ColumnType
import services.scalaexport.config.ExportModel
import services.scalaexport.inject.InjectSearchParams

object ServiceHelper {
  def writeSearchFields(model: ExportModel, file: ScalaFile, queriesFile: String, trace: String, searchArgs: String) = {
    file.add(s"override def countAll(creds: Credentials, filters: Seq[Filter] = Nil)$trace = {", 1)
    file.add(s"""traceB("get.all.count")(td => ApplicationDatabase.query($queriesFile.countAll(filters))(td))""")
    file.add("}", -1)
    file.add(s"override def getAll(creds: Credentials, $searchArgs)$trace = {", 1)
    file.add(s"""traceB("get.all")(td => ApplicationDatabase.query($queriesFile.getAll(filters, orderBys, limit, offset))(td))""")
    file.add("}", -1)
    file.add()
    file.add("// Search")
    file.add(s"override def searchCount(creds: Credentials, q: String, filters: Seq[Filter])$trace = {", 1)
    file.add(s"""traceB("search.count")(td => ApplicationDatabase.query($queriesFile.searchCount(q, filters))(td))""")
    file.add("}", -1)
    file.add(s"override def search(")
    file.add(s"  creds: Credentials, q: String, $searchArgs")
    file.add(s")$trace = {", 1)
    file.add(s"""traceB("search")(td => ApplicationDatabase.query($queriesFile.search(q, filters, orderBys, limit, offset))(td))""")
    file.add("}", -1)
    file.add()
    file.add(s"def searchExact(")
    file.add(s"  creds: Credentials, q: String, orderBys: Seq[OrderBy] = Nil, limit: Option[Int] = None, offset: Option[Int] = None")
    file.add(s")$trace = {", 1)
    file.add(s"""traceB("search.exact")(td => ApplicationDatabase.query($queriesFile.searchExact(q, orderBys, limit, offset))(td))""")
    file.add("}", -1)
    file.add()
  }

  private[this] val td = "(implicit trace: TraceData)"

  def addGetters(model: ExportModel, file: ScalaFile) = {
    model.pkFields.foreach(field => field.t.requiredImport.foreach(pkg => file.addImport(pkg, field.t.asScala)))
    model.pkFields match {
      case Nil => // noop
      case field :: Nil =>
        val colProp = field.propertyName
        file.add(s"def getByPrimaryKey(creds: Credentials, $colProp: ${field.t.asScala})$td = {", 1)
        file.add(s"""traceB("get.by.primary.key")(td => ApplicationDatabase.query(${model.className}Queries.getByPrimaryKey($colProp))(td))""")
        file.add("}", -1)
        val seqArgs = s"${colProp}Seq: Seq[${field.t.asScala}]"
        file.add(s"def getByPrimaryKeySeq(creds: Credentials, $seqArgs)$td = {", 1)
        file.add(s"""traceB("get.by.primary.key.seq")(td => ApplicationDatabase.query(${model.className}Queries.getByPrimaryKeySeq(${colProp}Seq))(td))""")
        file.add("}", -1)
        if (model.propertyName != "audit") {
          field.t match {
            case ColumnType.UuidType => file.addMarker("uuid-search", InjectSearchParams(model).toString)
            case ColumnType.IntegerType => file.addMarker("int-search", InjectSearchParams(model).toString)
            case _ => // noop
          }
        }
      case fields => // multiple columns
        val tupleTyp = "(" + fields.map(_.t.asScala).mkString(", ") + ")"
        val colArgs = fields.map(f => f.propertyName + ": " + f.t.asScala).mkString(", ")
        val queryArgs = fields.map(_.propertyName).mkString(", ")

        file.add(s"def getByPrimaryKey(creds: Credentials, $colArgs)$td = {", 1)
        file.add(s"""traceB("get.by.primary.key")(td => ApplicationDatabase.query(${model.className}Queries.getByPrimaryKey($queryArgs))(td))""")
        file.add("}", -1)

        file.add(s"def getByPrimaryKeySeq(creds: Credentials, pkSeq: Seq[$tupleTyp])$td = {", 1)
        file.add(s"""traceB("get.by.primary.key.seq")(td => ApplicationDatabase.query(${model.className}Queries.getByPrimaryKeySeq(pkSeq))(td))""")
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

        file.add(s"""def countBy$propCls(creds: Credentials, $propId: ${col.t.asScala})(implicit trace: TraceData) = traceB("count.by.$propId") { td =>""", 1)
        file.add(s"ApplicationDatabase.query(${model.className}Queries.CountBy$propCls($propId))(td)")
        file.add("}", -1)
        file.add(s"def getBy$propCls(creds: Credentials, $propId: ${col.t.asScala}, $searchArgs)(implicit trace: TraceData) = {", 1)
        file.add(s"""traceB("get.by.$propId")(td => ApplicationDatabase.query(${model.className}Queries.GetBy$propCls($propId, orderBys, limit, offset))(td))""")
        file.add("}", -1)
        file.add(s"""def getBy${propCls}Seq(creds: Credentials, ${propId}Seq: Seq[${col.t.asScala}])(implicit trace: TraceData) = traceB("get.by.$propId.seq") { td =>""", 1)
        file.add(s"ApplicationDatabase.query(${model.className}Queries.GetBy${propCls}Seq(${propId}Seq))(td)")
        file.add("}", -1)

        file.add()
      case _ => // noop
    }
  }
}
