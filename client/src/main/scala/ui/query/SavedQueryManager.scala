package ui.query

import java.util.UUID

import models.{ QueryDeleteRequest, QuerySaveRequest }
import models.query.SavedQuery
import models.template.Icons
import models.template.query.QueryEditorTemplate
import org.scalajs.jquery.{ jQuery => $ }
import ui.metadata.MetadataManager
import ui.modal.{ ConfirmManager, QueryExportFormManager, QuerySaveFormManager }
import ui.{ TabManager, UserManager }
import utils.NetworkMessage

object SavedQueryManager {
  var savedQueries = Map.empty[UUID, SavedQuery]
  var openSavedQueries = Set.empty[UUID]

  def deleteQuery(id: UUID) = {
    openSavedQueries = openSavedQueries - id
    savedQueries = savedQueries - id
    QueryManager.closeQuery(id)
    $("#saved-query-link-" + id).remove()
  }

  def updateSavedQueries(sqs: Seq[SavedQuery]) = {
    sqs.foreach { sq =>
      savedQueries = savedQueries + (sq.id -> sq)

      if (openSavedQueries(sq.id)) {
        utils.Logging.info(s"Closing query [${sq.id}].")
        openSavedQueries = openSavedQueries - sq.id
        QueryManager.closeQuery(sq.id)
        savedQueryDetail(sq.id)
      }
    }
    MetadataManager.updateSavedQueries(savedQueries.values.toSeq.sortBy(_.name))
  }

  def savedQueryDetail(id: UUID) = openSavedQueries.find(_ == id) match {
    case Some(queryId) =>
      TabManager.selectTab(id)
    case None =>
      val savedQuery = savedQueries.getOrElse(id, throw new IllegalStateException(s"Unknown saved query [$id]."))
      addSavedQuery(savedQuery)
      openSavedQueries = openSavedQueries + id
  }

  private[this] def addSavedQuery(savedQuery: SavedQuery) = {
    val engine = MetadataManager.engine.getOrElse(throw new IllegalStateException("No Engine"))
    QueryManager.workspace.append(QueryEditorTemplate.forSavedQuery(engine, savedQuery, UserManager.userId).toString)
    TabManager.addTab(savedQuery.id, "saved-query-" + savedQuery.id, savedQuery.name, Icons.savedQuery)

    val queryPanel = $(s"#panel-${savedQuery.id}")

    utils.JQueryUtils.clickHandler($(".export-link", queryPanel), (jq) => {
      QueryExportFormManager.show(savedQuery.id, QueryManager.getSql(savedQuery.id), savedQuery.name)
    })

    utils.JQueryUtils.clickHandler($(".settings-query-link", queryPanel), (jq) => QuerySaveFormManager.show(savedQuery.copy(
      sql = QueryManager.getSql(savedQuery.id)
    )))

    utils.JQueryUtils.clickHandler($(".save-as-query-link", queryPanel), (jq) => QuerySaveFormManager.show(savedQuery.copy(
      id = UUID.randomUUID,
      name = "Copy of " + savedQuery.name,
      sql = QueryManager.getSql(savedQuery.id)
    )))

    utils.JQueryUtils.clickHandler($(".save-query-link", queryPanel), (jq) => {
      val newSavedQuery = savedQuery.copy(
        sql = QueryManager.getSql(savedQuery.id)
      )
      NetworkMessage.sendMessage(QuerySaveRequest(newSavedQuery))
    })

    utils.JQueryUtils.clickHandler($(".delete-query-link", queryPanel), (jq) => {
      def callback(b: Boolean): Unit = {
        if (b) {
          NetworkMessage.sendMessage(QueryDeleteRequest(savedQuery.id))
        }
        ConfirmManager.close()
      }
      val msg = "Are you sure you want to delete this saved query?"
      ConfirmManager.show(callback = callback, content = msg, trueButton = "Delete")
    })

    def onChange(s: String): Unit = {
      if (s == savedQuery.sql) {
        $(".unsaved-status", queryPanel).css("display", "none")
      } else {
        $(".unsaved-status", queryPanel).css("display", "inline")
      }
    }

    def onClose() = {
      openSavedQueries = openSavedQueries - savedQuery.id
    }

    QueryManager.addQuery(savedQuery.id, savedQuery.name, queryPanel, onChange, onClose)
  }
}
