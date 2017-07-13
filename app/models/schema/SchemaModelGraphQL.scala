package models.schema

import models.graphql.GraphQLContext
import models.graphql.CommonGraphQL._
import models.result.QueryResultGraphQL._
import models.query.{QueryFilter, RowDataOptions}
import sangria.macros.derive._
import sangria.schema._
import services.query.RowDataService

object SchemaModelGraphQL {
  def rowDataOptionsFor(c: Context[GraphQLContext, _]) = {
    val sc = c.arg(sortColArg)
    val sa = c.arg(sortAscArg)
    val fc = c.arg(filterColArg)
    val fo = c.arg(filterOpArg)
    val ft = c.arg(filterTypeArg)
    val fv = c.arg(filterValueArg)
    val filters = fc match {
      case Some(col) => Seq(QueryFilter(col, fo.getOrElse(FilterOp.Equal), ft.getOrElse(ColumnType.StringType), fv.getOrElse("")))
      case None => Nil
    }
    val l = c.arg(limitArg)
    val o = c.arg(offsetArg)
    RowDataOptions(sc, sa, filters, l, o)
  }

  implicit val referenceType = deriveObjectType[GraphQLContext, Reference](ObjectTypeDescription("A reference to a different table or view."))
  implicit val primaryKeyType = deriveObjectType[GraphQLContext, PrimaryKey](ObjectTypeDescription("A primary key for this table or view."))
  implicit val foreignKeyType = deriveObjectType[GraphQLContext, ForeignKey](ObjectTypeDescription("A foreign key for this table or view."))

  implicit val indexColumnType = deriveObjectType[GraphQLContext, IndexColumn](ObjectTypeDescription("A column for this database index."))
  implicit val indexType = deriveObjectType[GraphQLContext, Index](ObjectTypeDescription("A database index for this table."))

  implicit val columnType = deriveObjectType[GraphQLContext, Column](ObjectTypeDescription("A database column for this table or view."))

  implicit val tableType = deriveObjectType[GraphQLContext, Table](
    ObjectTypeDescription("A database table for this connection."),
    AddFields(Field(
      name = "data",
      description = Some("Return this table's data, passing filters as arguments."),
      fieldType = resultType,
      arguments = resultArgs,
      resolve = c => {
        val columns = Seq("*") // TODO
        RowDataService.getRowData(c.ctx.user, c.value.connection, "table", c.value.name, columns, rowDataOptionsFor(c))
      }
    ))
  )

  implicit val viewType = deriveObjectType[GraphQLContext, View](
    ObjectTypeDescription("A database view for this connection."),
    AddFields(Field(
      name = "data",
      description = Some("Return this view's data, passing filters as arguments."),
      fieldType = resultType,
      arguments = resultArgs,
      resolve = c => RowDataService.getRowData(c.ctx.user, c.value.connection, "view", c.value.name, Nil, rowDataOptionsFor(c))
    ))
  )

  implicit val procedureParamType = deriveObjectType[GraphQLContext, ProcedureParam](ObjectTypeDescription("The parameter for this stored procedure."))
  implicit val procedureType = deriveObjectType[GraphQLContext, Procedure](ObjectTypeDescription("A stored procedure for this connection."))
}
