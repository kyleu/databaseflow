package ui.query

import java.util.UUID

import models.schema.EnumType
import models.template.Icons
import models.template.typ.EnumDetailTemplate
import org.scalajs.jquery.{jQuery => $}
import scribe.Logging
import ui.WorkspaceManager
import ui.metadata.MetadataManager
import ui.tabs.TabManager

object EnumManager extends Logging {
  var openEnums = Map.empty[String, UUID]

  def addEnum(e: EnumType) = {
    openEnums.get(e.key).foreach { key =>
      setEnumDetails(key, e)
    }
  }

  def enumDetail(key: String) = openEnums.get(key) match {
    case Some(queryId) => TabManager.selectTab(queryId)
    case None =>
      val queryId = UUID.randomUUID
      val enum = MetadataManager.schema.flatMap(_.enums.find(_.key == key)).getOrElse(throw new IllegalStateException(s"Cannot find enum [$key]."))
      WorkspaceManager.append(EnumDetailTemplate.forEnum(queryId, enum).toString)

      setEnumDetails(queryId, enum)

      def close() = if (QueryManager.closeQuery(queryId)) {
        openEnums = openEnums - key
      }

      TabManager.addTab(queryId, "enum-" + key, key, Icons.enum, close _)

      val queryPanel = $(s"#panel-$queryId")

      QueryManager.activeQueries = QueryManager.activeQueries :+ queryId

      openEnums = openEnums + (key -> queryId)
  }

  private[this] def setEnumDetails(uuid: UUID, enum: EnumType) = {
    val panel = $(s"#panel-$uuid")
    if (panel.length != 1) {
      throw new IllegalStateException(s"Missing enum panel for [$uuid].")
    }

    logger.debug(s"Enum [${enum.key}] loaded.")

    scalajs.js.Dynamic.global.$(".collapsible", panel).collapsible()
  }
}
