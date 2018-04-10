package ui.modal

import java.util.UUID

import models.RowDelete
import models.query.QueryResult
import models.template.tbl.RowDetailTemplate
import org.scalajs.jquery.{jQuery => $}
import ui.metadata.MetadataManager
import util.{NetworkMessage, TemplateUtils}

import scala.scalajs.js

object RowDetailManager {
  private[this] var activeTable: Option[String] = None
  private[this] var activePk: Seq[String] = Nil
  private[this] var activeData: Seq[(QueryResult.Col, String)] = Nil

  private[this] val modal = js.Dynamic.global.$("#row-detail-modal")

  private[this] val modalContent = $("#row-detail-modal-content", modal)
  private[this] val linkDelete = $("#row-detail-delete-link", modal)
  private[this] val linkOk = $("#row-detail-ok-link", modal)
  private[this] val linkEdit = $("#row-detail-edit-link", modal)

  def init() = {
    TemplateUtils.clickHandler(linkDelete, _ => delete())
    TemplateUtils.clickHandler(linkEdit, _ => edit())
    TemplateUtils.clickHandler(linkOk, _ => close())
  }

  def show(table: Option[String], pk: Seq[String], data: Seq[(QueryResult.Col, String)]) = {
    activeTable = table
    activePk = pk
    activeData = data

    val html = RowDetailTemplate.forData(data)
    modalContent.html(html.render)
    if (table.isEmpty || pk.isEmpty) {
      linkEdit.hide()
      linkDelete.hide()
    } else {
      linkEdit.show()
      linkDelete.show()
    }
    modal.openModal()
  }

  private[this] def edit() = {
    val name = activeTable.getOrElse(throw new IllegalStateException("No active table"))
    val columns = MetadataManager.schema.flatMap(_.tables.find(_.name == name)).map(_.columns).getOrElse(Nil)
    val keyData = activePk.flatMap { col =>
      activeData.find(_._1.name.equalsIgnoreCase(col)).map(_._2)
    }
    val pk = activePk.zip(keyData)
    RowUpdateManager.show(insert = false, name, pk, columns, activeData.map(x => x._1.name -> x._2).toMap)
    close()
  }

  private[this] def delete() = {
    val name = activeTable.getOrElse(throw new IllegalStateException("No active table"))
    val columns = MetadataManager.schema.flatMap(_.tables.find(_.name == name)).map(_.columns).getOrElse(Nil)
    val keyData = activePk.flatMap { col =>
      activeData.find(_._1.name.equalsIgnoreCase(col)).map(_._2)
    }
    val pk = activePk.zip(keyData)
    val msg = s"Are you sure you wish to delete the [$name] row matching [${pk.map(x => x._1 + " = " + x._2).mkString(", ")}]?"
    if (org.scalajs.dom.window.confirm(msg)) {
      NetworkMessage.sendMessage(RowDelete(name, pk, UUID.randomUUID))
    }
  }

  private[this] def close() = {
    modalContent.text("")
    modal.closeModal()
  }
}
