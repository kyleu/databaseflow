package com.databaseflow.services.scalaexport.db.file

import com.databaseflow.models.scalaexport.db.ExportModel
import com.databaseflow.models.scalaexport.db.config.ExportConfiguration
import com.databaseflow.models.scalaexport.file.ScalaFile

object ResultFile {
  private[this] val resultArgs = "paging = paging, filters = filters, orderBys = orderBys, totalCount = totalCount, results = results, durationMs = durationMs"

  def export(config: ExportConfiguration, model: ExportModel) = {
    val root = config.modelLocationOverride.orElse(if (model.scalaJs) { Some(ScalaFile.sharedSrc) } else { None })
    val file = ScalaFile(pkg = model.modelPackage, key = model.className + "Result", root = root, core = true)

    file.addImport("java.time", "LocalDateTime")
    file.addImport(config.providedPrefix + "models.result", "BaseResult")
    file.addImport(config.providedPrefix + "models.result.filter", "Filter")
    file.addImport(config.providedPrefix + "models.result.orderBy", "OrderBy")
    file.addImport(config.providedPrefix + "models.result.paging", "PagingOptions")

    file.addImport(config.providedPrefix + "util.JsonSerializers", "_")

    file.add(s"final case class ${model.className}Result(", 2)
    file.add("override val filters: Seq[Filter] = Nil,")
    file.add("override val orderBys: Seq[OrderBy] = Nil,")
    file.add("override val totalCount: Int = 0,")
    file.add("override val paging: PagingOptions = PagingOptions(),")
    file.add(s"override val results: Seq[${model.className}] = Nil,")
    file.add("override val durationMs: Int = 0,")
    file.add(s"override val occurred: LocalDateTime = ${config.providedPrefix}util.DateUtils.now")
    file.add(s") extends BaseResult[${model.className}]", -2)

    file.add()
    file.add(s"object ${model.className}Result {", 1)
    file.add(s"implicit val jsonEncoder: Encoder[${model.className}Result] = deriveEncoder")
    file.add(s"implicit val jsonDecoder: Decoder[${model.className}Result] = deriveDecoder")
    file.add()

    file.add("def fromRecords(")
    file.add("  q: Option[String], filters: Seq[Filter] = Nil, orderBys: Seq[OrderBy] = Nil, limit: Option[Int] = None, offset: Option[Int] = None,")
    file.add(s"  startMs: Long, totalCount: Int, results: Seq[${model.className}]")
    file.add(") = {", 1)
    file.add("val paging = PagingOptions.from(totalCount, limit, offset)")
    file.add(s"val durationMs = (${config.providedPrefix}util.DateUtils.nowMillis - startMs).toInt")
    file.add(s"${model.className}Result($resultArgs)")
    file.add("}", -1)
    file.add("}", -1)

    file
  }
}
