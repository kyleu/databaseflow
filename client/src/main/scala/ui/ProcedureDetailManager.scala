package ui

import java.util.UUID

import models.schema.Procedure
import models.template.ProcedureDetailTemplate
import models.{ RequestMessage, ShowTable }
import org.scalajs.jquery.{ JQueryEventObject, jQuery => $ }

object ProcedureDetailManager {
  var openProcedures = Map.empty[String, UUID]

  def procedureDetail(procedure: Procedure, sendMessage: (RequestMessage) => Unit) = openProcedures.get(procedure.name) match {
    case Some(queryId) =>
      TabManager.selectTab(queryId)
    case None =>
      val queryId = UUID.randomUUID
      TabManager.initIfNeeded()
      WorkspaceManager.append(ProcedureDetailTemplate.forProcedure(queryId, procedure).toString)

      TabManager.addTab(queryId, procedure.name, "code")

      val queryPanel = $(s"#panel-$queryId")

      QueryManager.activeQueries = QueryManager.activeQueries :+ queryId

      $(".call-link", queryPanel).click({ (e: JQueryEventObject) =>
        viewData(queryId, procedure, sendMessage)
        false
      })

      $(".fa-close", queryPanel).click({ (e: JQueryEventObject) =>
        openProcedures = openProcedures - procedure.name
        QueryManager.closeQuery(queryId, None, sendMessage)
        false
      })

      openProcedures = openProcedures + (procedure.name -> queryId)
  }

  private[this] def viewData(queryId: UUID, procedure: Procedure, sendMessage: (RequestMessage) => Unit) = {
    sendMessage(ShowTable(queryId = queryId, name = procedure.name))
  }
}
