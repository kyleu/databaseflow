package services.scalaexport.file

import models.scalaexport.ScalaFile
import services.scalaexport.ExportTable

object ResultFile {
  def export(et: ExportTable) = {
    val file = ScalaFile("models" +: et.pkg, et.className + "Result")

    file.addImport("java.time", "LocalDateTime")
    file.addImport("models.result", "BaseResult")
    file.addImport("models.result.filter", "Filter")
    file.addImport("models.result.orderBy", "OrderBy")
    file.addImport("models.result.paging", "PagingOptions")

    file.add(s"case class ${et.className}Result(", 1)
    file.add("override val filters: Seq[Filter] = Nil,")
    file.add("override val orderBys: Seq[OrderBy] = Nil,")
    file.add("override val totalCount: Int = 0,")
    file.add("override val paging: PagingOptions = PagingOptions(),")
    file.add(s"override val records: Seq[${et.className}] = Nil,")
    file.add("override val durationMs: Int = 0,")
    file.add("override val occurred: LocalDateTime = util.DateUtils.now")
    file.add(s") extends BaseResult[${et.className}]", -1)
    file
  }
}
