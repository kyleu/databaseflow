package services.explore

import models.graphql.{ColumnGraphQL, CommonGraphQL, ForeignKeyGraphQL, GraphQLContext}
import models.result.{QueryResultGraphQL, QueryResultRow}
import models.schema.{SchemaModelGraphQL, Table}
import sangria.execution.deferred.HasId
import sangria.schema._
import services.query.QueryResultRowService

object ExploreTableHelper {
  val emptyObjectType = ObjectType(
    name = "empty",
    description = "Empty table used as a placeholder when things go wrong",
    fieldsFn = () => fields[GraphQLContext, QueryResultRow](Field(
    name = "unavailable",
    fieldType = StringType,
    resolve = (_: Context[GraphQLContext, QueryResultRow]) => "unavailable"
  ))
  )

  def getTables(schema: models.schema.Schema) = {
    val schemaTables = schema.tables.filter(_.columns.nonEmpty)
    if (schemaTables.isEmpty) {
      None
    } else {
      var tableTypes: Option[Seq[(Table, ObjectType[GraphQLContext, QueryResultRow])]] = None

      def tableFieldset(src: Table) = {
        val columnFields = src.columns.map { col =>
          ColumnGraphQL.getColumnField(CommonGraphQL.cleanName(col.name), col.name, col.description, col.columnType, col.notNull, col.sqlTypeName)
        }
        val fkFields = src.foreignKeys.map { fk =>
          val types = tableTypes.getOrElse(throw new IllegalStateException("No available table types."))
          val tgt = types.find(_._1.name.equalsIgnoreCase(fk.targetTable)).map(_._2).getOrElse(emptyObjectType)
          val notNull = fk.references.forall(ref => src.columns.exists(col => col.name == ref.source && col.notNull))
          ForeignKeyGraphQL.getForeignKeyField(schema, src, tgt, notNull, fk)
        }
        fields[GraphQLContext, QueryResultRow](columnFields ++ fkFields: _*)
      }

      tableTypes = Some(schemaTables.map { table =>
        table -> ObjectType(
          name = CommonGraphQL.cleanName(table.name),
          description = table.description.getOrElse(s"Table [${table.name}]"),
          fieldsFn = () => tableFieldset(table)
        )
      })

      val tableFields = {
        val types = tableTypes.getOrElse(throw new IllegalStateException("No available table types."))
        fields[GraphQLContext, Unit](types.map { t =>
          Field(
            name = t._1.name,
            fieldType = ListType(t._2),
            description = t._1.description,
            arguments = QueryResultGraphQL.resultArgs,
            resolve = Projector(1, (x: Context[GraphQLContext, Unit], names) => {
              val noRels = names.forall(n => t._1.columns.exists(_.name == n.name))
              val columns = if (noRels) { names.map(_.name) } else { Nil }
              QueryResultRowService.getTableData(x.ctx.user, schema.connectionId, t._1.name, columns, SchemaModelGraphQL.rowDataOptionsFor(x))
            })
          )
        }: _*)
      }

      Some(ObjectType(name = "tables", description = "The tables contained in this schema.", fields = tableFields))
    }
  }
}
