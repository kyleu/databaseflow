package ui.query

import java.util.UUID

import models.{CloseQuery, QueryDeleteRequest, QuerySaveRequest}
import models.query.SavedQuery
import models.template.Icons
import models.template.query.QueryEditorTemplate
import org.scalajs.jquery.{jQuery => $}
import ui.metadata.MetadataManager
import ui.modal.{ConfirmManager, SavedQueryFormManager}
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
    SavedQueryFormManager.modal.closeModal()
    $("#saved-query-link-" + id).remove()
  }

  def updateSavedQueryDetail(sq: SavedQuery) = {
    val panel = $(s"#panel-${sq.id}")
    if (panel.length != 1) {
      throw new IllegalStateException(s"Encountered [${panel.length}] panels for saved query [${sq.id}].")
    }
    $(".query-title", panel).text(sq.name)
    SqlManager.setSql(sq.id, sq.sql)
  }

  def updateSavedQueries(sqs: Seq[SavedQuery], usernames: Map[UUID, String]) = {
    usernameMap = usernameMap ++ usernames
    sqs.foreach { sq =>
      savedQueries = savedQueries + (sq.id -> sq)
      if (openSavedQueries(sq.id)) {
        updateSavedQueryDetail(sq)
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
    val userId = UserManager.userId.getOrElse(throw new IllegalStateException("Missing user details."))
    QueryManager.workspace.append(QueryEditorTemplate.forSavedQuery(engine, savedQuery, userId).toString)

    def close() = if (QueryManager.activeQueries.contains(savedQuery.id)) {
      QueryManager.closeQuery(savedQuery.id)
      openSavedQueries = openSavedQueries - savedQuery.id
      NetworkMessage.sendMessage(CloseQuery(savedQuery.id))
    }

    TabManager.addTab(savedQuery.id, "saved-query-" + savedQuery.id, savedQuery.name, Icons.savedQuery, close)

    val queryPanel = $(s"#panel-${savedQuery.id}")
    TemplateUtils.clickHandler($(".settings-query-link", queryPanel), jq => SavedQueryFormManager.show(savedQuery.copy(
      sql = SqlManager.getSql(savedQuery.id)
    )))
    TemplateUtils.clickHandler($(".save-as-query-link", queryPanel), jq => SavedQueryFormManager.show(savedQuery.copy(
      id = UUID.randomUUID,
      name = "Copy of " + savedQuery.name,
      sql = SqlManager.getSql(savedQuery.id),
      params = ParameterManager.getParamsOpt(savedQuery.id)
    )))
    TemplateUtils.clickHandler($(".save-query-link", queryPanel), jq => {
      val newSavedQuery = savedQuery.copy(
        sql = SqlManager.getSql(savedQuery.id),
        params = ParameterManager.getParamsOpt(savedQuery.id)
      )
      NetworkMessage.sendMessage(QuerySaveRequest(newSavedQuery))
    })
    TemplateUtils.clickHandler($(".delete-query-link", queryPanel), jq => {
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

    QueryManager.addQuery(savedQuery.id, savedQuery.name, queryPanel, savedQuery.sql, savedQuery.params.getOrElse(Map.empty), onChange)
  }
}
