package ui.query

import java.util.UUID

import models.query.SharedResult
import models.template.Icons
import models.template.results.SharedResultTemplate
import org.scalajs.jquery.{jQuery => $}
import services.ChartService
import ui.TabManager
import ui.metadata.MetadataManager

object SharedResultManager {
  var sharedResults = Map.empty[UUID, SharedResult]
  var openSharedResults = Set.empty[UUID]
  var usernameMap = Map.empty[UUID, String]

  def updateSharedResults(srs: Seq[SharedResult], usernames: Map[UUID, String]) = {
    usernameMap = usernameMap ++ usernames
    srs.foreach { sr =>
      sharedResults = sharedResults + (sr.id -> sr)
    }
    MetadataManager.updateSharedResults(sharedResults.values.toSeq.sortBy(_.title))
  }

  def sharedResultDetail(id: UUID) = openSharedResults.find(_ == id) match {
    case Some(queryId) => TabManager.selectTab(id)
    case None =>
      addSharedResult(sharedResults.getOrElse(id, throw new IllegalStateException(s"Unknown shared result [$id].")))
      openSharedResults = openSharedResults + id
  }

  private[this] def addSharedResult(sr: SharedResult) = {
    QueryManager.workspace.append(SharedResultTemplate.forSharedResult(sr).toString)

    def close() = if (QueryManager.activeQueries.contains(sr.id)) {
      QueryManager.closeQuery(sr.id)
      openSharedResults = openSharedResults - sr.id
    }

    TabManager.addTab(sr.id, "shared-result-" + sr.id, sr.title, Icons.sharedResult, close)

    ChartService.init()

    QueryManager.activeQueries = QueryManager.activeQueries :+ sr.id
  }
}
