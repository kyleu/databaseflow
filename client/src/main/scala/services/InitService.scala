package services

import java.net.URLDecoder
import java.util.UUID

import models.RequestMessage
import org.scalajs.dom
import org.scalajs.jquery.{jQuery => $}
import services.query.TransactionService
import ui._
import ui.metadata.{MetadataManager, ModelListManager}
import ui.modal._
import ui.query._
import ui.search.SearchManager
import utils.{Logging, NetworkMessage, TemplateUtils}

import scala.scalajs.js

object InitService {
  def init(sendMessage: (RequestMessage) => Unit, connect: () => Unit): Unit = {
    Logging.installErrorHandler()
    NetworkMessage.register(sendMessage)
    wireSideNav()
    installTimers()

    TemplateUtils.clickHandler($("#commit-button"), jq => TransactionService.commitTransaction())
    TemplateUtils.clickHandler($("#rollback-button"), jq => TransactionService.rollbackTransaction())

    js.Dynamic.global.$("select").material_select()

    EditorCreationHelper.initEditorFramework()
    SearchManager.init()

    ShortcutService.init()
    ConfirmManager.init()
    ReconnectManager.init()
    SavedQueryFormManager.init()
    RowDetailManager.init()
    RowUpdateManager.init()
    SharedResultFormManager.init()
    QueryExportFormManager.init()
    PlanNodeDetailManager.init()
    Logging.debug("Database Flow has started.")
    connect()
  }

  private[this] def wireSideNav() = {
    TemplateUtils.clickHandler($("#begin-tx-link"), jq => TransactionService.beginTransaction())
    TemplateUtils.clickHandler($("#new-query-link"), jq => AdHocQueryManager.addNewQuery())
    TemplateUtils.clickHandler($(".show-list-link"), jq => ModelListManager.showList(jq.data("key").toString))
    TemplateUtils.clickHandler($("#sidenav-graphql-link"), jq => GraphQLManager.show())
    TemplateUtils.clickHandler($("#sidenav-help-link"), jq => HelpManager.show())
    TemplateUtils.clickHandler($("#sidenav-feedback-link"), jq => FeedbackManager.show())
    TemplateUtils.clickHandler($("#sidenav-refresh-link"), jq => MetadataManager.refreshSchema())
    TemplateUtils.clickHandler($("#sidenav-history-link"), jq => HistoryManager.show())
    js.Dynamic.global.$(".button-collapse").sideNav()
  }

  def performInitialAction() = {
    TabManager.initIfNeeded()
    NavigationService.initialMessage match {
      case ("graphql", None) => GraphQLManager.show()
      case ("help", None) => HelpManager.show()
      case ("feedback", None) => FeedbackManager.show()
      case ("history", None) => HistoryManager.show()
      case ("list", Some(key)) => ModelListManager.showList(key)
      case ("new", None) => AdHocQueryManager.addNewQuery()
      case ("new", Some(id)) => AdHocQueryManager.addNewQuery(queryId = UUID.fromString(id))
      case ("saved-query", Some(id)) => SavedQueryManager.savedQueryDetail(UUID.fromString(id))
      case ("shared-result", Some(id)) => SharedResultManager.sharedResultDetail(UUID.fromString(id))
      case ("table", Some(id)) => TableManager.forString(id)
      case ("view", Some(id)) => ViewManager.viewDetail(id)
      case ("procedure", Some(id)) => ProcedureManager.procedureDetail(id)
      case ("sql", Some(sql)) => AdHocQueryManager.addNewQuery(initialSql = Some(URLDecoder.decode(sql, "UTF-8")))
      case (key, id) =>
        Logging.info(s"Unhandled initial message [$key:${id.getOrElse("")}].")
        AdHocQueryManager.addNewQuery()
    }
  }

  def installTimers() = {
    dom.window.setInterval(TemplateUtils.relativeTime _, 1000)
  }
}
