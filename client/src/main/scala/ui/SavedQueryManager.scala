package ui

import java.util.UUID

import models.RequestMessage
import models.query.SavedQuery
import models.template.{ Icons, QueryEditorTemplate }
import org.scalajs.jquery.{ JQueryEventObject, jQuery => $ }

import scala.scalajs.js

object SavedQueryManager {
  var savedQueries = Map.empty[UUID, SavedQuery]
  var openSavedQueries = Set.empty[UUID]

  def savedQueryDetail(id: UUID, sendMessage: (RequestMessage) => Unit) = openSavedQueries.find(_ == id) match {
    case Some(queryId) =>
      TabManager.selectTab(id)
    case None =>
      val savedQuery = savedQueries.getOrElse(id, throw new IllegalStateException(s"Unknown saved query [$id]."))
      addSavedQuery(savedQuery, sendMessage)
      openSavedQueries = openSavedQueries + id
  }

  private[this] def addSavedQuery(savedQuery: SavedQuery, sendMessage: (RequestMessage) => Unit) = {
    QueryManager.workspace.append(QueryEditorTemplate.forSavedQuery(savedQuery.id, savedQuery.title, savedQuery.sql).toString)
    TabManager.addTab(savedQuery.id, savedQuery.title, Icons.savedQuery)

    val queryPanel = $(s"#panel-${savedQuery.id}")
    $(s".save-as-query-link", queryPanel).click({ (e: JQueryEventObject) =>
      val modal = js.Dynamic.global.$("#save-query-modal")
      utils.Logging.info(modal.length.toString)
      modal.openModal()
      false
    })

    def onChange(s: String): Unit = {
      if (s == savedQuery.sql) {
        $(".unsaved-status", queryPanel).hide()
      } else {
        $(".unsaved-status", queryPanel).show()
      }
    }

    def onClose() = {
      openSavedQueries = openSavedQueries - savedQuery.id
    }

    QueryManager.addQuery(savedQuery.id, queryPanel, sendMessage, onChange, onClose)
  }
}
