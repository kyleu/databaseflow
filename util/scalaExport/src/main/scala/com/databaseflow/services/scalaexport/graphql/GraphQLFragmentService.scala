package com.databaseflow.services.scalaexport.graphql

import com.databaseflow.models.scalaexport.file.ScalaFile
import com.databaseflow.models.scalaexport.graphql.GraphQLExportConfig
import sangria.ast._
import sangria.schema.Schema

object GraphQLFragmentService {
  import GraphQLQueryParseService._

  def fragmentFile(cfg: GraphQLExportConfig, n: String, d: FragmentDefinition, nameMap: Map[String, ClassName], schema: Schema[_, _]) = {
    if (nameMap(n).provided) {
      None
    } else {
      val cn = nameMap(n)
      val file = ScalaFile(cn.pkg, cn.cn, Some(""))
      file.addImport(cfg.providedPrefix + "util.JsonSerializers", "_")

      meaningfulComments(d.comments).foreach(c => file.add("// " + c))

      GraphQLObjectHelper.objectFor(cfg, file, cn, schema.allTypes(d.typeCondition.name), d.selections, nameMap, schema, incEncoder = true)

      Some(file)
    }
  }
}
