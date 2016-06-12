package ui

import org.scalajs.jquery.{jQuery => $}

object WorkspaceManager {
  lazy val workspace = $("#workspace")

  def append(html: String) = {
    workspace.append(html)
  }
}
