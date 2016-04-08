package services.connection

import models._
import services.schema.SchemaService

trait DetailHelper { this: ConnectionService =>
  protected[this] def handleGetTableDetail(name: String) = {
    val startMs = System.currentTimeMillis
    val table = SchemaService.getTable(connectionId, name)
    table.foreach { t =>
      out ! TableResultResponse(Seq(t), (System.currentTimeMillis - startMs).toInt)
    }
  }

  protected[this] def handleGetViewDetail(name: String) = {
    val startMs = System.currentTimeMillis
    val view = SchemaService.getView(connectionId, name)
    view.foreach { v =>
      out ! ViewResultResponse(Seq(v), (System.currentTimeMillis - startMs).toInt)
    }
  }

  protected[this] def handleGetProcedureDetail(name: String) = {
    val startMs = System.currentTimeMillis
    val procedure = SchemaService.getProcedure(connectionId, name)
    procedure.foreach { p =>
      out ! ProcedureResultResponse(Seq(p), (System.currentTimeMillis - startMs).toInt)
    }
  }
}
