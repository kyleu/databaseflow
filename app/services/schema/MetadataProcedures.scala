package services.schema

import java.sql.DatabaseMetaData

import models.database.Row
import models.queries.QueryTranslations
import models.schema.{ Procedure, ProcedureParam }
import utils.NullUtils

object MetadataProcedures {
  def getProcedures(metadata: DatabaseMetaData, catalog: Option[String], schema: Option[String]) = {
    val rs = metadata.getProcedures(catalog.orNull, schema.orNull, NullUtils.inst)
    val procedures = new Row.Iter(rs).map(procedureFromRow).toList.sortBy(_.name)
    procedures.map { p =>
      getProcedureDetails(metadata, catalog, schema, p)
    }
  }

  def getProcedureDetails(metadata: DatabaseMetaData, catalog: Option[String], schema: Option[String], procedure: Procedure) = {
    val rs2 = metadata.getProcedureColumns(catalog.orNull, schema.orNull, procedure.name, NullUtils.inst)
    val columns = new Row.Iter(rs2).map(columnFromRow).toList.sortBy(_._1).map(_._2)
    procedure.copy(params = columns)
  }

  private[this] def procedureFromRow(row: Row) = Procedure(
    name = row.as[String]("procedure_name"),
    description = row.asOpt[String]("remarks"),
    params = Nil,
    returnsResult = row.as[Int]("procedure_type") match {
      case DatabaseMetaData.procedureResultUnknown => None
      case DatabaseMetaData.procedureReturnsResult => Some(true)
      case DatabaseMetaData.procedureNoResult => Some(false)
      case x => throw new IllegalArgumentException(x.toString)
    }
  )

  private[this] def columnFromRow(row: Row) = {
    val paramType = row.as[Any]("COLUMN_TYPE") match {
      case i: Int => i
      case s: String => s.toInt
    }

    row.as[Int]("ORDINAL_POSITION") -> ProcedureParam(
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
      columnType = QueryTranslations.forType(row.as[Int]("DATA_TYPE")),
      sqlTypeCode = row.as[Int]("DATA_TYPE"), // SQL_DATA_TYPE? SOURCE_DATA_TYPE?
      sqlTypeName = row.as[String]("TYPE_NAME"),
      nullable = row.as[String]("IS_NULLABLE") == "YES"
    )
  }
}
