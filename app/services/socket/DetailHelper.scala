package services.socket

import models._
import models.schema.{Procedure, Table, View}
import services.database.DatabaseWorkerPool
import services.schema.{MetadataProcedures, MetadataTables, MetadataViews, SchemaService}
import utils.ExceptionUtils

trait DetailHelper { this: SocketService =>
  protected[this] def handleGetTableDetail(name: String) = SchemaService.getTable(connectionId, name).foreach { t =>
    def work() = db.withConnection { conn =>
      MetadataTables.withTableDetails(db, conn, conn.getMetaData, Seq(t))
    }
    def onSuccess(tables: Seq[Table]) = {
      out ! TableResultResponse(tables)
    }
    def onFailure(x: Throwable) = {
      ExceptionUtils.actorErrorFunction(out, "TableDetail", x)
    }
    DatabaseWorkerPool.submitWork(work, onSuccess, onFailure)
  }

  protected[this] def handleGetViewDetail(name: String) = SchemaService.getView(connectionId, name).foreach { v =>
    def work() = db.withConnection { conn =>
      MetadataViews.withViewDetails(db, conn, conn.getMetaData, Seq(v))
    }
    def onSuccess(views: Seq[View]) = {
      out ! ViewResultResponse(views)
    }
    def onFailure(x: Throwable) = {
      ExceptionUtils.actorErrorFunction(out, "ViewDetail", x)
    }
    DatabaseWorkerPool.submitWork(work, onSuccess, onFailure)
  }

  protected[this] def handleGetProcedureDetail(name: String) = SchemaService.getProcedure(connectionId, name).foreach { p =>
    def work() = db.withConnection { conn =>
      MetadataProcedures.withProcedureDetails(conn.getMetaData, Option(conn.getCatalog), Option(conn.getSchema), Seq(p))
    }
    def onSuccess(procedures: Seq[Procedure]) = {
      out ! ProcedureResultResponse(procedures)
    }
    def onFailure(x: Throwable) = {
      ExceptionUtils.actorErrorFunction(out, "ProcedureDetail", x)
    }
    DatabaseWorkerPool.submitWork(work, onSuccess, onFailure)
  }

  protected[this] def handleSocketTrace() {
    val ret = SocketTraceResponse(id, user.id, user.username)
    sender() ! ret
  }

  protected[this] def handleClientTrace() {
    pendingDebugChannel = Some(sender())
    out ! SendTrace
  }

  protected[this] def handleDebugInfo(data: String) = pendingDebugChannel match {
    case Some(dc) =>
      val json = upickle.json.read(data)
      dc ! ClientTraceResponse(id, json)
    case None =>
      log.warn(s"Received unsolicited DebugInfo [$data] from [$id] with no active connection.")
  }
}
