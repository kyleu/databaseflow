package services.schema

import java.sql.DatabaseMetaData

import models.database.Row
import models.schema.{ FunctionParam, DatabaseFunction }
import utils.NullUtils

object MetadataFunctions {
  def getFunctions(metadata: DatabaseMetaData, catalog: Option[String], schema: Option[String]) = {
    val rs = metadata.getFunctions(catalog.orNull, schema.orNull, NullUtils.inst)

    val functions = new Row.Iter(rs).flatMap { row =>
      if(row.hasColumn("FUNCTION_NAME")) {
        val name = row.as[String]("FUNCTION_NAME")
        val description = row.asOpt[String]("REMARKS")
        val f = DatabaseFunction(
          name = name,
          description = description
        )
        Some(f)
      } else {
        None
      }
    }.toList

    val functionsWithParams = functions.map { f =>
      val rs = metadata.getFunctionColumns(catalog.orNull, schema.orNull, f.name, NullUtils.inst)
      val columns = new Row.Iter(rs).map { row =>
        val paramType = row.as[Any]("COLUMN_TYPE") match {
          case i: Int => i
          case s: String => s.toInt
        }

        row.as[Int]("ORDINAL_POSITION") -> FunctionParam(
          name = row.as[String]("COLUMN_NAME"),
          description = row.asOpt[String]("REMARKS"),
          paramType = paramType match {
            case DatabaseMetaData.procedureColumnUnknown => "unknown"
            case DatabaseMetaData.procedureColumnIn => "in"
            case DatabaseMetaData.procedureColumnInOut => "inout"
            case DatabaseMetaData.procedureColumnOut => "out"
            case DatabaseMetaData.procedureColumnReturn => "return"
            case DatabaseMetaData.procedureColumnResult => "result"
            case x => "?"
          },
          typeCode = row.as[Int]("DATA_TYPE"), // SQL_DATA_TYPE? SOURCE_DATA_TYPE?
          typeName = row.as[String]("TYPE_NAME"),
          nullable = row.as[String]("IS_NULLABLE") == "YES"
        )
      }.toList.sortBy(_._1).map(_._2)
      f.copy(params = columns)
    }

    functionsWithParams.sortBy(_.name)
  }
}
