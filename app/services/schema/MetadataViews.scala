package services.schema

import java.sql.{Connection, DatabaseMetaData}
import java.util.UUID

import models.database.{Query, Row}
import models.engine.DatabaseEngine.MySQL
import models.schema.{EnumType, View}
import services.database.DatabaseConnection
import util.{Logging, NullUtils}

import scala.util.control.NonFatal

object MetadataViews extends Logging {
  def getViews(connectionId: UUID, metadata: DatabaseMetaData, catalog: Option[String], schema: Option[String]) = {
    val rs = metadata.getTables(catalog.orNull, schema.orNull, NullUtils.inst, Array("VIEW"))
    new Row.Iter(rs).map(row => fromRow(connectionId, row)).toList.sortBy(_.name)
  }

  def withViewDetails(db: DatabaseConnection, conn: Connection, metadata: DatabaseMetaData, views: Seq[View], enums: Seq[EnumType]) = views.map { view =>
    getViewDetails(db, conn, metadata, view, enums)
  }

  private[this] def getViewDetails(db: DatabaseConnection, conn: Connection, metadata: DatabaseMetaData, view: View, enums: Seq[EnumType]) = try {
    val definition = db.engine match {
      case MySQL => Some(db(conn, new Query[String] {
        override val sql = "show create view " + view.name
        override def reduce(rows: Iterator[Row]) = rows.map(_.as[String]("Create View")).toList.headOption.getOrElse {
          throw new IllegalStateException("Missing [Create View] column.")
        }
      }))
      case _ => None
    }

    view.copy(
      definition = definition,
      columns = MetadataColumns.getColumns(metadata, view.catalog, view.schema, view.name, enums)
    )
  } catch {
    case NonFatal(x) =>
      log.info(s"Unable to get view details for [${view.name}].", x)
      view
  }

  private[this] def fromRow(connectionId: UUID, row: Row) = View(
    name = row.as[String]("TABLE_NAME"),
    connection = connectionId,
    catalog = row.asOpt[String]("TABLE_CAT"),
    schema = row.asOpt[String]("TABLE_SCHEM"),
    description = row.asOpt[String]("REMARKS"),
    definition = None
  )
}
