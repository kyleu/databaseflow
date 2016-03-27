package ui

import models.query.SavedQuery
import models.schema.{ Procedure, Table }
import org.scalajs.jquery.{ JQuery, JQueryEventObject, jQuery => $ }
import utils.Logging

object SearchManager {
  private[this] lazy val searchContainer = $(".search-wrapper")
  private[this] lazy val searchInput = $("input#search", searchContainer)

  private[this] lazy val savedQueriesToggle = $("#saved-query-list-toggle")
  private[this] lazy val tablesToggle = $("#table-list-toggle")
  private[this] lazy val viewsToggle = $("#view-list-toggle")
  private[this] lazy val proceduresToggle = $("#procedure-list-toggle")

  private[this] var currentSearch = ""

  def init() = {
    if (searchContainer.length != 1 || searchInput.length != 1) {
      throw new IllegalStateException("Missing search input field.")
    }

    searchInput.keyup { (e: JQueryEventObject) =>
      onTextChange(searchInput.value().toString)
    }
    searchInput.blur { (e: JQueryEventObject) =>
      onTextChange(searchInput.value().toString)
    }
  }

  def onTextChange(search: String) = if (currentSearch != search && MetadataManager.schema.isDefined) {
    val searches = search.toLowerCase.split(" ").map(_.trim).filter(_.nonEmpty)
    if (searches.isEmpty) {
      clearSearch()
    } else {
      Logging.info(s"Searching [$search]...")
      filterSchema(searches)
    }
    currentSearch = search
  }

  private[this] def openIfClosed(j: JQuery) = if (!j.hasClass("active")) {
    $(".collapsible-header", j).trigger("click")
  }

  private[this] def closeIfOpen(j: JQuery) = if (j.hasClass("active")) {
    $(".collapsible-header", j).trigger("click")
  }

  private[this] def clearSearch() = {
    $(".saved-query-link, .table-link, .view-link, .procedure-link").removeClass("search-ignored")
    closeIfOpen(savedQueriesToggle)
    closeIfOpen(tablesToggle)
    closeIfOpen(viewsToggle)
    closeIfOpen(proceduresToggle)
    MetadataManager.savedQueries.foreach(_.foreach(_._2.show()))
    MetadataManager.tables.foreach(_.foreach(_._2.show()))
    MetadataManager.views.foreach(_.foreach(_._2.show()))
    MetadataManager.procedures.foreach(_.foreach(_._2.show()))
  }

  private[this] def filterObjects[T](key: String, seq: Seq[(T, JQuery)], searches: Seq[String], matchFunc: (Seq[String], T) => Boolean, toggle: JQuery) = {
    val (matched, notMatched) = seq.partition { t =>
      matchFunc(searches, t._1)
    }
    matched.foreach(_._2.show())
    notMatched.foreach(_._2.hide())
    if (matched.isEmpty) {
      closeIfOpen(toggle)
    } else {
      openIfClosed(toggle)
    }
    Logging.info(s"Matched [${matched.size}] and skipped [${notMatched.size}] ${key}s.")

  }

  private[this] def matchSavedQuery(searches: Seq[String], sq: SavedQuery) = searches.forall(s => sq.title.toLowerCase.contains(s))
  private[this] def matchTable(searches: Seq[String], t: Table) = searches.forall(s => t.name.toLowerCase.contains(s))
  private[this] def matchProcedure(searches: Seq[String], p: Procedure) = searches.forall(s => p.name.toLowerCase.contains(s))

  private[this] def filterSchema(searches: Seq[String]) = {
    MetadataManager.savedQueries.foreach { savedQueries =>
      filterObjects("saved-query", savedQueries, searches, matchSavedQuery, savedQueriesToggle)
    }
    MetadataManager.tables.map { tables =>
      filterObjects("table", tables, searches, matchTable, tablesToggle)
    }
    MetadataManager.views.map { views =>
      filterObjects("view", views, searches, matchTable, viewsToggle)
    }
    MetadataManager.procedures.map { procedures =>
      filterObjects("procedure", procedures, searches, matchProcedure, proceduresToggle)
    }
  }
}
