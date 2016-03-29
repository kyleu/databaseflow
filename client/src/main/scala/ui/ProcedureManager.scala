package ui

import java.util.UUID

import models.RequestMessage
import models.schema.Procedure
import models.template.{ Icons, ProcedureDetailTemplate }
import org.scalajs.jquery.{ JQueryEventObject, jQuery => $ }
import services.NotificationService

object ProcedureManager {
  var procedures = Map.empty[String, Procedure]
  var openProcedures = Map.empty[String, UUID]

  def procedureDetail(name: String, sendMessage: (RequestMessage) => Unit) = openProcedures.get(name) match {
    case Some(queryId) =>
      TabManager.selectTab(queryId)
    case None =>
      val queryId = UUID.randomUUID
      TabManager.initIfNeeded()
      WorkspaceManager.append(ProcedureDetailTemplate.forProcedure(queryId, name).toString)

      TabManager.addTab(queryId, name, Icons.procedure)

      val queryPanel = $(s"#panel-$queryId")

      QueryManager.activeQueries = QueryManager.activeQueries :+ queryId

      $(".call-link", queryPanel).click({ (e: JQueryEventObject) =>
        procedures.get(name) match {
          case Some(procedure) => callProcedure(queryId, procedure, sendMessage)
          case None => NotificationService.info("Procedure Not Loaded", "Please retry in a moment.")
        }
        false
      })

      $(s".${Icons.close}", queryPanel).click({ (e: JQueryEventObject) =>
        openProcedures = openProcedures - name
        QueryManager.closeQuery(queryId, None, sendMessage)
        false
      })

      openProcedures = openProcedures + (name -> queryId)
  }

  private[this] def callProcedure(queryId: UUID, procedure: Procedure, sendMessage: (RequestMessage) => Unit) = {
    //sendMessage(???)
  }
}
