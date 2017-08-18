package services.scalaexport.file

import models.scalaexport.ScalaFile
import services.scalaexport.config.ExportModel

object ResultFile {
  private[this] val resultArgs = "paging = paging, filters = filters, orderBys = orderBys, totalCount = totalCount, results = results, durationMs = durationMs"

  def export(model: ExportModel) = {
    val file = ScalaFile(model.modelPackage, model.className + "Result")

    file.addImport("java.time", "LocalDateTime")
    file.addImport("models.result", "BaseResult")
    file.addImport("models.result.filter", "Filter")
    file.addImport("models.result.orderBy", "OrderBy")
    file.addImport("models.result.paging", "PagingOptions")

    file.add(s"case class ${model.className}Result(", 1)
    file.add("override val filters: Seq[Filter] = Nil,")
    file.add("override val orderBys: Seq[OrderBy] = Nil,")
    file.add("override val totalCount: Int = 0,")
    file.add("override val paging: PagingOptions = PagingOptions(),")
    file.add(s"override val results: Seq[${model.className}] = Nil,")
    file.add("override val durationMs: Int = 0,")
    file.add("override val occurred: LocalDateTime = util.DateUtils.now")
    file.add(s") extends BaseResult[${model.className}]", -1)

    file.add()
    file.add(s"object ${model.className}Result {", 1)
    file.add("def fromRecords(")
    file.add("  q: Option[String], filters: Seq[Filter], orderBys: Seq[OrderBy], limit: Option[Int], offset: Option[Int],")
    file.add(s"  startMs: Long, totalCount: Int, results: Seq[${model.className}]")
    file.add(") = {", 1)
    file.add("val paging = PagingOptions.from(totalCount, limit, offset)")
    file.add("val durationMs = (System.currentTimeMillis - startMs).toInt")
    file.add(s"${model.className}Result($resultArgs)")
    file.add("}", -1)
    file.add("}", -1)

    file
  }
}
