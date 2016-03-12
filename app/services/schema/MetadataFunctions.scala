package services.schema

import java.sql.DatabaseMetaData

import models.database.Row
import models.schema.DatabaseFunction
import utils.NullUtils

object MetadataFunctions {
  def getFunctions(metadata: DatabaseMetaData, catalog: Option[String], schema: Option[String]) = {
    val rs = metadata.getFunctions(catalog.orNull, schema.orNull, NullUtils.inst)
    val functions = new Row.Iter(rs).map { row =>
      val name = row.as[String]("function_name")
      val description = row.asOpt[String]("remarks")
      DatabaseFunction(
        name = name,
        description = description
      )
    }.toList

    functions.sortBy(_.name)
  }
}
