package com.databaseflow.services.scalaexport.graphql

import com.databaseflow.models.scalaexport.file.ScalaFile
import com.databaseflow.models.scalaexport.graphql.GraphQLExportConfig
import sangria.ast._
import sangria.schema.Schema

object GraphQLFragmentService {
  import GraphQLQueryParseService._

  def fragmentFile(cfg: GraphQLExportConfig, n: String, d: FragmentDefinition, nameMap: Map[String, ClassName], schema: Option[Schema[_, _]]) = {
    if (nameMap(n).provided) {
      None
    } else {
      val cn = nameMap(n)
      val file = ScalaFile(cn.pkg, cn.cn, Some(""))
      file.addImport(cfg.rootPrefix + "util.JsonSerializers", "_")

      meaningfulComments(d.comments).foreach(c => file.add("// " + c))

      file.add(s"object ${cn.cn} {", 1)
      file.add(s"implicit val jsonDecoder: Decoder[${cn.cn}] = deriveDecoder")
      file.add(s"implicit val jsonEncoder: Encoder[${cn.cn}] = deriveEncoder")
      file.add("}", -1)

      file.add(s"case class ${cn.cn}(", 2)
      val typ = schema.flatMap(_.allTypes.get(d.typeCondition.name))
      GraphQLQueryHelper.addFields(cfg.rootPrefix, cfg.modelPkg, file, cn.pkg, typ, d.selections, nameMap)
      file.add(")", -2)

      Some(file)
    }
  }
}
