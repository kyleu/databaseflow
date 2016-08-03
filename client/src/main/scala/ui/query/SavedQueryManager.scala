package ui.query

import java.util.UUID

import models.{QueryDeleteRequest, QuerySaveRequest}
import models.query.SavedQuery
import models.template.Icons
import models.template.query.QueryEditorTemplate
import org.scalajs.jquery.{jQuery => $}
import ui.metadata.MetadataManager
import ui.modal.{ConfirmManager, QueryExportFormManager, QuerySaveFormManager}
import ui.{TabManager, UserManager}
import utils.{NetworkMessage, TemplateUtils}

object SavedQueryManager {
  var savedQueries = Map.empty[UUID, SavedQuery]
  var openSavedQueries = Set.empty[UUID]
  var usernameMap = Map.empty[UUID, String]

  def deleteQuery(id: UUID) = {
    openSavedQueries = openSavedQueries - id
    savedQueries = savedQueries - id
    QueryManager.closeQuery(id)
    $("#saved-query-link-" + id).remove()
  }

  def updateSavedQueries(sqs: Seq[SavedQuery], usernames: Map[UUID, String]) = {
    usernameMap = usernameMap ++ usernames
    sqs.foreach { sq =>
      savedQueries = savedQueries + (sq.id -> sq)
      if (openSavedQueries(sq.id)) {
        SqlManager.setSql(sq.id, sq.sql)
      }
    }
    MetadataManager.updateSavedQueries(savedQueries.values.toSeq.sortBy(_.name))
  }

  def savedQueryDetail(id: UUID) = openSavedQueries.find(_ == id) match {
    case Some(queryId) => TabManager.selectTab(id)
    case None =>
      addSavedQuery(savedQueries.getOrElse(id, throw new IllegalStateException(s"Unknown saved query [$id].")))
      openSavedQueries = openSavedQueries + id
  }

  private[this] def addSavedQuery(savedQuery: SavedQuery) = {
    val engine = MetadataManager.engine.getOrElse(throw new IllegalStateException("No Engine"))
    QueryManager.workspace.append(QueryEditorTemplate.forSavedQuery(engine, savedQuery, UserManager.userId).toString)

    def close() = {
      QueryManager.closeQuery(savedQuery.id)
      openSavedQueries = openSavedQueries - savedQuery.id
    }

    TabManager.addTab(savedQuery.id, "saved-query-" + savedQuery.id, savedQuery.name, Icons.savedQuery, close)

    val queryPanel = $(s"#panel-${savedQuery.id}")
    TemplateUtils.clickHandler($(".export-link", queryPanel), (jq) => {
      QueryExportFormManager.show(savedQuery.id, SqlManager.getSql(savedQuery.id), savedQuery.name)
    })
    TemplateUtils.clickHandler($(".settings-query-link", queryPanel), (jq) => QuerySaveFormManager.show(savedQuery.copy(
      sql = SqlManager.getSql(savedQuery.id)
    )))
    TemplateUtils.clickHandler($(".save-as-query-link", queryPanel), (jq) => QuerySaveFormManager.show(savedQuery.copy(
      id = UUID.randomUUID,
      name = "Copy of " + savedQuery.name,
      sql = SqlManager.getSql(savedQuery.id)
    )))
    TemplateUtils.clickHandler($(".save-query-link", queryPanel), (jq) => {
      val newSavedQuery = savedQuery.copy(
        sql = SqlManager.getSql(savedQuery.id)
      )
      NetworkMessage.sendMessage(QuerySaveRequest(newSavedQuery))
    })
    TemplateUtils.clickHandler($(".delete-query-link", queryPanel), (jq) => {
      def callback(b: Boolean): Unit = {
        if (b) {
          NetworkMessage.sendMessage(QueryDeleteRequest(savedQuery.id))
        }
        ConfirmManager.close()
      }
      val msg = "Are you sure you want to delete this saved query?"
      ConfirmManager.show(callback = callback, content = msg, trueButton = "Delete")
    })

    val runQueryLink = $(".run-query-link", queryPanel)
    val runQueryAllLink = $(".run-query-all-link", queryPanel)

    def onChange(s: String): Unit = {
      $(".unsaved-status", queryPanel).css("display", if (s == savedQuery.sql) { "none" } else { "inline" })
      SqlManager.updateLinks(savedQuery.id, runQueryLink, runQueryAllLink)
    }

    QueryManager.addQuery(savedQuery.id, savedQuery.name, queryPanel, onChange)
  }
}
