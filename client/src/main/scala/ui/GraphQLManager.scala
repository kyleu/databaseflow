package ui

import java.util.UUID

import models.template.{GraphQLTemplate, Icons}
import org.scalajs.jquery.{jQuery => $}
import ui.query.QueryManager

import scalatags.Text.all._

object GraphQLManager {
  private[this] val graphqlId = UUID.fromString("99999999-9999-9999-9999-999999999999")
  private[this] var isOpen = false

  def show() = {
    if (isOpen) {
      TabManager.selectTab(graphqlId)
    } else {
      val panelHtml = div(id := s"panel-$graphqlId", cls := "workspace-panel")(GraphQLTemplate.panel)
      WorkspaceManager.append(panelHtml.toString)

      def close() = {
        isOpen = false
        QueryManager.closeQuery(graphqlId)
      }

      TabManager.addTab(graphqlId, "graphql", "GraphQL", Icons.graphQL, close)
      QueryManager.activeQueries = QueryManager.activeQueries :+ graphqlId

      val queryPanel = $(s"#panel-$graphqlId")

      isOpen = true
    }
  }
}
