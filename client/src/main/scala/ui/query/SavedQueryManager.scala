package ui.query

import java.util.UUID

import models.query.SavedQuery
import org.scalajs.jquery.{jQuery => $}
import ui.metadata.MetadataManager
import ui.modal.SavedQueryFormManager
import ui.tabs.TabManager

object SavedQueryManager extends SavedQueryChangeManager {
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
    case Some(_) => TabManager.selectTab(id)
    case None =>
      addSavedQuery(savedQueries.getOrElse(id, throw new IllegalStateException(s"Unknown saved query [$id].")))
      openSavedQueries = openSavedQueries + id
  }
}
