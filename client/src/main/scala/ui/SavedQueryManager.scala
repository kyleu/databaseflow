package ui

import java.util.UUID

import models.RequestMessage
import models.query.SavedQuery

object SavedQueryManager {
  var openSavedQueries = Set.empty[UUID]

  def savedQueryDetail(savedQuery: SavedQuery, sendMessage: (RequestMessage) => Unit) = openSavedQueries.find(_ == savedQuery.id) match {
    case Some(queryId) =>
      TabManager.selectTab(savedQuery.id)
    case None =>
      QueryManager.addSavedQuery(savedQuery, sendMessage, () => {
        openSavedQueries = openSavedQueries - savedQuery.id
      })
      openSavedQueries = openSavedQueries + savedQuery.id
  }
}
