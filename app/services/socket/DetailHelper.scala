package services.socket

import models._
import models.queries.column.ColumnDetailQueries
import models.schema._
import services.database.DatabaseWorkerPool
import services.schema.{MetadataProcedures, MetadataTables, MetadataViews, SchemaService}
import util.ExceptionUtils
import util.JsonSerializers._

trait DetailHelper { this: SocketService =>
  protected[this] def handleGetTableDetail(name: String, enums: Seq[EnumType]) = SchemaService.getTable(connectionId, name).foreach { t =>
    def work() = db.withConnection { conn =>
      MetadataTables.withTableDetails(db, conn, conn.getMetaData, Seq(t), enums)
    }
    def onSuccess(tables: Seq[Table]) = {
      out ! TableResponse(tables)
    }
    def onFailure(x: Throwable) = {
      ExceptionUtils.actorErrorFunction(out, "TableDetail", x)
    }
    DatabaseWorkerPool.submitWork(work _, onSuccess, onFailure)
  }

  protected[this] def handleGetProcedureDetail(name: String, enums: Seq[EnumType]) = SchemaService.getProcedure(connectionId, name).foreach { p =>
    def work() = db.withConnection { conn =>
      MetadataProcedures.withProcedureDetails(conn.getMetaData, Option(conn.getCatalog), Option(conn.getSchema), Seq(p), enums)
    }
    def onSuccess(procedures: Seq[Procedure]) = {
      out ! ProcedureResponse(procedures)
    }
    def onFailure(x: Throwable) = {
      ExceptionUtils.actorErrorFunction(out, "ProcedureDetail", x)
    }
    DatabaseWorkerPool.submitWork(work _, onSuccess, onFailure)
  }

  protected[this] def handleGetViewDetail(name: String, enums: Seq[EnumType]) = SchemaService.getView(connectionId, name).foreach { v =>
    def work() = db.withConnection { conn =>
      MetadataViews.withViewDetails(db, conn, conn.getMetaData, Seq(v), enums)
    }
    def onSuccess(views: Seq[View]) = {
      out ! ViewResponse(views)
    }
    def onFailure(x: Throwable) = {
      ExceptionUtils.actorErrorFunction(out, "ViewDetail", x)
    }
    DatabaseWorkerPool.submitWork(work _, onSuccess, onFailure)
  }

  protected[this] def handleGetColumnDetail(owner: String, name: String, t: String) = {
    import models.schema.ColumnType._
    val colType = withNameInsensitive(t)
    def work() = if (colType.isNumeric) {
      db.query(ColumnDetailQueries.NumberColumnDetail(db.engine, owner, name))
    } else {
      db.query(ColumnDetailQueries.BasicColumnDetail(owner, name))
    }
    def onSuccess(value: ColumnDetails) = {
      out ! ColumnDetailResponse(owner, name, value)
    }
    def onFailure(x: Throwable) = {
      ExceptionUtils.actorErrorFunction(out, "ColumnDetail", x)
    }
    DatabaseWorkerPool.submitWork(work _, onSuccess, onFailure)
  }

  protected[this] def handleSocketTrace() = {
    val ret = SocketTraceResponse(id, user.id, user.username)
    sender() ! ret
  }

  protected[this] def handleClientTrace() = {
    pendingDebugChannel = Some(sender())
    out ! SendTrace
  }

  protected[this] def handleDebugInfo(data: String) = pendingDebugChannel match {
    case Some(dc) =>
      val json = parseJson(data).right.get
      dc ! ClientTraceResponse(id, json)
    case None => log.warn(s"Received unsolicited DebugInfo [$data] from [$id] with no active connection.")
  }
}
