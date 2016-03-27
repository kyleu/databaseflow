package services.connection

import models._
import services.schema.SchemaService

trait DetailHelper { this: ConnectionService =>
  protected[this] def handleGetTableDetail(name: String) = {
    val startMs = System.currentTimeMillis
    val table = SchemaService.getTableDetail(connectionId, db, name)
    out ! TableResultResponse(table, (System.currentTimeMillis - startMs).toInt)
  }

  protected[this] def handleGetViewDetail(name: String) = {
    val startMs = System.currentTimeMillis
    val view = SchemaService.getViewDetail(connectionId, db, name)
    out ! ViewResultResponse(view, (System.currentTimeMillis - startMs).toInt)
  }

  protected[this] def handleGetProcedureDetail(name: String) = {
    val startMs = System.currentTimeMillis
    val procedure = SchemaService.getProcedureDetail(connectionId, db, name)
    out ! ProcedureResultResponse(procedure, (System.currentTimeMillis - startMs).toInt)
  }
}
