package com.databaseflow.services.scalaexport.graphql

import com.databaseflow.models.scalaexport.file.ScalaFile
import com.databaseflow.models.scalaexport.graphql.GraphQLExportConfig
import com.databaseflow.services.scalaexport.ExportHelper
import com.databaseflow.services.scalaexport.graphql.GraphQLQueryParseService.ClassName
import sangria.schema.EnumValue

object GraphQLEnumService {
  private[this] val forbidden = Set("__directivelocation", "__typekind")

  def enumFile(cfg: GraphQLExportConfig, name: String, values: Seq[EnumValue[_]], nameMap: Map[String, ClassName]) = {
    if (nameMap.get(name).exists(_.provided)) {
      None
    } else if (forbidden(name.toLowerCase)) {
      None
    } else {
      val cn = nameMap(name)
      val file = ScalaFile(cn.pkg, cn.cn, Some(""))
      file.addImport("enumeratum.values", "_")

      file.add(s"sealed abstract class ${cn.cn}(override val value: String) extends StringEnumEntry")
      file.add()
      file.add(s"object ${cn.cn} extends StringEnum[${cn.cn}] {", 1)
      values.foreach { v =>
        file.add(s"""case object ${ExportHelper.toClassName(v.value.toString)} extends ${cn.cn}("${v.value}")""")
      }
      file.add()
      file.add("override val values = findValues")
      file.add("}", -1)

      Some(file)
    }
  }
}
