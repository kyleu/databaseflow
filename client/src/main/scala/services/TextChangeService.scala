package services

import java.util.UUID

import org.scalajs.dom
import org.scalajs.dom.BeforeUnloadEvent
import utils.NullUtils

object TextChangeService {
  private[this] val dirtyEditors = collection.mutable.HashSet.empty[UUID]

  def init() = dom.window.onbeforeunload = (e: BeforeUnloadEvent) => {
    if (dirtyEditors.isEmpty) {
      NullUtils.inst
    } else {
      "Changes you made may not be saved."
    }
  }

  def markDirty(id: UUID) = if (!dirtyEditors(id)) {
    dirtyEditors += id
  }

  def markClean(id: UUID) = if (dirtyEditors(id)) {
    dirtyEditors -= id
  }

  def shouldClose(id: UUID) = if (dirtyEditors(id)) {
    if (dom.window.confirm("You have unsaved changes. Close this query?")) {
      dirtyEditors -= id
      true
    } else {
      false
    }
  } else {
    true
  }
}
