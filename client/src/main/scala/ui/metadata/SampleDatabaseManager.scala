package ui.metadata

import java.util.UUID

import models.template.{Icons, StaticPanelTemplate}
import models.{BatchQueryStatus, CreateSampleDatabase}
import org.scalajs.jquery.{jQuery => $}
import services.NotificationService
import ui.modal.ConfirmManager
import ui.TabManager
import ui.query.QueryManager
import utils.NetworkMessage

import scalatags.Text.all._

object SampleDatabaseManager {
  var activeSample: Option[UUID] = None

  def createSample(): Unit = {
    val queryId = UUID.randomUUID
    activeSample = Some(queryId)
    val content = div(
      div("Completed SQL statements: ", div(cls := "completed-statements")("0")),
      div("Remaining SQL statements: ", div(cls := "remaining-statements")("0"))
    )
    QueryManager.workspace.append(StaticPanelTemplate.panel(queryId, span("Loading Sample Database"), content, Icons.database).render)
    TabManager.addTab(queryId, "ctx-load", "Sample Database", Icons.database, () => Unit)
    NetworkMessage.sendMessage(CreateSampleDatabase(queryId))
    ConfirmManager.close()
  }

  def process(bqs: BatchQueryStatus) = activeSample.map { queryId =>
    val queryPanel = $(s"#panel-$queryId")
    $(".completed-statements", queryPanel).text(bqs.completedQueries.toString)
    $(".remaining-statements", queryPanel).text(bqs.remainingQueries.toString)
    if (bqs.remainingQueries == 0) {
      NotificationService.info("DatabaseLoaded", "The sample database was imported successfully.")
      TabManager.removeTab(queryId)
      queryPanel.remove()
    }
  }
}
