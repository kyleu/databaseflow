package services.schema

import java.sql.DatabaseMetaData

import models.database.Row
import models.schema.{ Procedure, ProcedureParam }
import utils.NullUtils

object MetadataProcedures {
  def getProcedures(metadata: DatabaseMetaData, catalog: Option[String], schema: Option[String]) = {
    val rs = metadata.getProcedures(catalog.orNull, schema.orNull, NullUtils.inst)
    val procedures = new Row.Iter(rs).map { row =>
      val name = row.as[String]("procedure_name")
      val description = row.asOpt[String]("remarks")
      val returnsResult = row.as[Int]("procedure_type") match {
        case DatabaseMetaData.procedureResultUnknown => None
        case DatabaseMetaData.procedureReturnsResult => Some(true)
        case DatabaseMetaData.procedureNoResult => Some(false)
        case x => throw new IllegalArgumentException(x.toString)
      }
      Procedure(
        name = name,
        description = description,
        params = Nil,
        returnsResult = returnsResult
      )

    }.toList

    val proceduresWithParams = procedures.map { p =>
      val rs = metadata.getProcedureColumns(catalog.orNull, schema.orNull, p.name, NullUtils.inst)
      val columns = new Row.Iter(rs).map { row =>
        row.as[Int]("ORDINAL_POSITION") -> ProcedureParam(
          name = row.as[String]("COLUMN_NAME"),
          description = row.asOpt[String]("REMARKS"),
          paramType = row.as[Int]("COLUMN_TYPE") match {
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
      p.copy(params = columns)
    }

    proceduresWithParams.sortBy(_.name)
  }
}
