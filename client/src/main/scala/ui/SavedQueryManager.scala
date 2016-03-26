package ui

import java.util.UUID

import models.RequestMessage
import models.query.SavedQuery
import models.schema.Table
import models.template.SavedQueryDetailTemplate
import org.scalajs.jquery.{JQueryEventObject, jQuery => $}

object SavedQueryManager {
  var openSavedQueries = Map.empty[UUID, UUID]

  def savedQueryDetail(savedQuery: SavedQuery, sendMessage: (RequestMessage) => Unit) = openSavedQueries.get(savedQuery.id) match {
    case Some(queryId) =>
      TabManager.selectTab(queryId)
    case None =>
      val queryId = UUID.randomUUID
      TabManager.initIfNeeded()
      WorkspaceManager.append(SavedQueryDetailTemplate.forSavedQuery(queryId, savedQuery).toString)

      TabManager.addTab(queryId, savedQuery.title, "envelope-o")

      val queryPanel = $(s"#panel-$queryId")

      QueryManager.activeQueries = QueryManager.activeQueries :+ queryId

      $(".fa-close", queryPanel).click({ (e: JQueryEventObject) =>
        openSavedQueries = openSavedQueries - savedQuery.id
        QueryManager.closeQuery(queryId, None, sendMessage)
        false
      })

      openSavedQueries = openSavedQueries + (savedQuery.id -> queryId)
  }

  private[this] def viewData(queryId: UUID, view: Table, sendMessage: (RequestMessage) => Unit) = {
    //sendMessage(???)
  }
}
