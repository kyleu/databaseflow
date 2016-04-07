package ui

import java.util.UUID

import models.schema.Table
import models.template._
import org.scalajs.jquery.{ JQuery, JQueryEventObject, jQuery => $ }
import services.NotificationService

object TableObjectManager {
  def wire(queryPanel: JQuery, queryId: UUID, name: String) = {
    def crash() = NotificationService.info("Table Not Loaded", "Please retry in a moment.")

    $(".foreign-keys-link", queryPanel).click({ (e: JQueryEventObject) =>
      MetadataManager.schema.flatMap(_.tables.find(_.name == name)) match {
        case Some(table) => viewForeignKeys(queryId, table)
        case None => crash()
      }
      false
    })

    $(".indexes-link", queryPanel).click({ (e: JQueryEventObject) =>
      MetadataManager.schema.flatMap(_.tables.find(_.name == name)) match {
        case Some(table) => viewIndexes(queryId, table)
        case None => crash()
      }
      false
    })

    $(".columns-link", queryPanel).click({ (e: JQueryEventObject) =>
      MetadataManager.schema.flatMap(_.tables.find(_.name == name)) match {
        case Some(table) => viewColumns(queryId, table)
        case None => crash()
      }
      false
    })

    $(".definition-link", queryPanel).click({ (e: JQueryEventObject) =>
      MetadataManager.schema.flatMap(_.tables.find(_.name == name)) match {
        case Some(table) => viewDefinition(queryId, table)
        case None => crash()
      }
      false
    })
  }

  private[this] def viewForeignKeys(queryId: UUID, table: Table) = {
    val id = UUID.randomUUID
    val html = TableForeignKeyDetailTemplate.foreignKeysForTable(id, queryId, table)
    $(s"#workspace-$queryId").prepend(html.toString)
    $(s"#$id .${Icons.close}").click({ (e: JQueryEventObject) =>
      $(s"#$id").remove()
      false
    })
  }

  private[this] def viewIndexes(queryId: UUID, table: Table) = {
    val id = UUID.randomUUID
    val html = TableIndexDetailTemplate.indexesForTable(id, queryId, table)
    $(s"#workspace-$queryId").prepend(html.toString)
    $(s"#$id .${Icons.close}").click({ (e: JQueryEventObject) =>
      $(s"#$id").remove()
      false
    })
  }

  private[this] def viewColumns(queryId: UUID, table: Table) = {
    val id = UUID.randomUUID
    val html = TableColumnDetailTemplate.columnsForTable(id, queryId, table)
    $(s"#workspace-$queryId").prepend(html.toString)
    $(s"#$id .${Icons.close}").click({ (e: JQueryEventObject) =>
      $(s"#$id").remove()
      false
    })
  }

  private[this] def viewDefinition(queryId: UUID, table: Table) = {
    val id = UUID.randomUUID
    val html = TableDefinitionTemplate.definitionForTable(id, queryId, table)
    $(s"#workspace-$queryId").prepend(html.toString)
    $(s"#$id .${Icons.close}").click({ (e: JQueryEventObject) =>
      $(s"#$id").remove()
      false
    })
  }
}
