package ui.query

import java.util.UUID

import models.GetProcedureDetail
import models.schema.Procedure
import models.template.{ Icons, ProcedureDetailTemplate }
import org.scalajs.jquery.{ jQuery => $ }
import services.NotificationService
import ui.metadata.MetadataManager
import ui.{ TabManager, WorkspaceManager }

object ProcedureManager {
  var openProcedures = Map.empty[String, UUID]

  def addProcedure(p: Procedure) = {
    openProcedures.get(p.name).foreach { uuid =>
      setProcedureDetails(uuid, p)
    }
  }

  def procedureDetail(name: String) = openProcedures.get(name) match {
    case Some(queryId) =>
      TabManager.selectTab(queryId)
    case None =>
      val queryId = UUID.randomUUID
      WorkspaceManager.append(ProcedureDetailTemplate.forProcedure(queryId, name).toString)

      MetadataManager.schema.flatMap(_.procedures.find(_.name == name)) match {
        case Some(procedure) if procedure.params.nonEmpty => setProcedureDetails(queryId, procedure)
        case _ => utils.NetworkMessage.sendMessage(GetProcedureDetail(name))
      }

      TabManager.addTab(queryId, "procedure-" + name, name, Icons.procedure)

      val queryPanel = $(s"#panel-$queryId")

      QueryManager.activeQueries = QueryManager.activeQueries :+ queryId

      utils.JQueryUtils.clickHandler($(".call-link", queryPanel), (jq) => {
        MetadataManager.schema.flatMap(_.procedures.find(_.name == name)) match {
          case Some(procedure) => callProcedure(queryId, procedure)
          case None => NotificationService.info("Procedure Not Loaded", "Please retry in a moment.")
        }
      })

      utils.JQueryUtils.clickHandler($(s".${Icons.close}", queryPanel), (jq) => {
        openProcedures = openProcedures - name
        QueryManager.closeQuery(queryId)
      })

      openProcedures = openProcedures + (name -> queryId)
  }

  private[this] def callProcedure(queryId: UUID, procedure: Procedure) = {
    utils.Logging.info(s"Calling procedure [$queryId]: " + procedure)
    //utils.NetworkMessage.sendMessage(???)
  }

  private[this] def setProcedureDetails(uuid: UUID, proc: Procedure) = {
    val panel = $(s"#panel-$uuid")
    if (panel.length != 1) {
      throw new IllegalStateException(s"Missing procedure panel for [$uuid].")
    }

    proc.description.map { desc =>
      $(".description", panel).text(desc)
    }

    utils.Logging.debug(s"Procedure [${proc.name}] loaded.")
  }
}
