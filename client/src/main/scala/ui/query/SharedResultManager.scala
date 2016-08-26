package ui.query

import java.util.UUID

import models.query.SharedResult
import ui.metadata.MetadataManager

object SharedResultManager {
  var sharedResults = Map.empty[UUID, SharedResult]
  var usernameMap = Map.empty[UUID, String]

  def updateSharedResults(srs: Seq[SharedResult], usernames: Map[UUID, String]) = {
    usernameMap = usernameMap ++ usernames
    srs.foreach { sr =>
      sharedResults = sharedResults + (sr.id -> sr)
    }
    MetadataManager.updateSharedResults(sharedResults.values.toSeq.sortBy(_.title))
  }

  def sharedResultDetail(id: UUID) = {
    val url = s"/shared/$id"
    org.scalajs.dom.window.open(url, "_blank")
  }
}
