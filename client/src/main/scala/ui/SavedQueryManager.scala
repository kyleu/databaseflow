package ui

import java.util.UUID

import models.QueryDeleteRequest
import models.query.SavedQuery
import models.template.{ Icons, QueryEditorTemplate }
import org.scalajs.jquery.{ JQueryEventObject, jQuery => $ }
import utils.NetworkMessage

object SavedQueryManager {
  var savedQueries = Map.empty[UUID, SavedQuery]
  var openSavedQueries = Set.empty[UUID]

  def savedQueryDetail(id: UUID) = openSavedQueries.find(_ == id) match {
    case Some(queryId) =>
      TabManager.selectTab(id)
    case None =>
      val savedQuery = savedQueries.getOrElse(id, throw new IllegalStateException(s"Unknown saved query [$id]."))
      addSavedQuery(savedQuery)
      openSavedQueries = openSavedQueries + id
  }

  private[this] def addSavedQuery(savedQuery: SavedQuery) = {
    val userId = UserManager.userId.getOrElse(throw new IllegalStateException("Not signed in."))
    QueryManager.workspace.append(QueryEditorTemplate.forSavedQuery(savedQuery, userId).toString)
    TabManager.addTab(savedQuery.id, savedQuery.name, Icons.savedQuery)

    val queryPanel = $(s"#panel-${savedQuery.id}")

    $(s".save-as-query-link", queryPanel).click({ (e: JQueryEventObject) =>
      QueryFormManager.show(savedQuery.copy(
        id = UUID.randomUUID,
        name = "Copy of " + savedQuery.name,
        sql = QueryManager.getSql(savedQuery.id)
      ))
      false
    })

    $(s".delete-query-link", queryPanel).click({ (e: JQueryEventObject) =>
      def callback(b: Boolean): Unit = {
        if (b) {
          NetworkMessage.sendMessage(QueryDeleteRequest(savedQuery.id))
        }
        ConfirmManager.close()
      }
      val msg = "Are you sure you want to delete this saved query?"
      ConfirmManager.show(callback = callback, content = msg, trueButton = "Delete")
      false
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

    QueryManager.addQuery(savedQuery.id, queryPanel, onChange, onClose)
  }
}
