package ui

import models.template.Icons
import org.scalajs.jquery.{ JQuery, JQueryEventObject, jQuery => $ }

object SearchManager {
  private[this] lazy val searchContainer = $(".search-wrapper")
  private[this] lazy val searchInput = $("input#search", searchContainer)
  private[this] lazy val searchIcon = $(".fa", searchContainer)

  lazy val savedQueriesToggle = $("#saved-query-list-toggle")
  lazy val tablesToggle = $("#table-list-toggle")
  lazy val viewsToggle = $("#view-list-toggle")
  lazy val proceduresToggle = $("#procedure-list-toggle")

  private[this] var currentSearch = ""

  def init() = {
    if (searchContainer.length != 1 || searchInput.length != 1) {
      throw new IllegalStateException("Missing search input field.")
    }

    searchIcon.click { (e: JQueryEventObject) =>
      searchInput.value("")
      clearSearch()
      searchInput.trigger("focus")
      searchInput.trigger("blur")
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
      if (searchIcon.hasClass(Icons.close)) {
        searchIcon.removeClass(Icons.close).addClass(Icons.search).css("pointer", "default")
      }
      clearSearch()
    } else {
      //utils.Logging.info(s"Searching [$search]...")
      if (searchIcon.hasClass(Icons.search)) {
        searchIcon.removeClass(Icons.search).addClass(Icons.close).css("pointer", "cursor")
      }
      SearchFilterManager.filterSchema(searches)
    }
    currentSearch = search
  }

  def openIfClosed(j: JQuery) = if (!j.hasClass("active")) {
    $(".collapsible-header", j).trigger("click")
  }
  def closeIfOpen(j: JQuery) = if (j.hasClass("active")) {
    $(".collapsible-header", j).trigger("click")
  }

  def clearSearchEntries(o: Option[scala.Seq[(String, JQuery, JQuery)]], toggle: JQuery) = {
    closeIfOpen(toggle)
    o.foreach(_.foreach { x =>
      x._3.text(x._1)
      x._2.show()
    })
  }

  private[this] def clearSearch() = {
    $(".saved-query-link, .table-link, .view-link, .procedure-link").removeClass("search-ignored")
    closeIfOpen(savedQueriesToggle)
    clearSearchEntries(MetadataManager.savedQueries.map(_.map(x => (x._1.id.toString, x._2, x._3))), savedQueriesToggle)
    clearSearchEntries(MetadataManager.tables, tablesToggle)
    clearSearchEntries(MetadataManager.views, viewsToggle)
    clearSearchEntries(MetadataManager.procedures, proceduresToggle)
  }

  private[this] def highlightMatches(title: String, matches: Seq[String], j: JQuery) = {
    val replaced = matches.foldLeft(title) { (x, y) =>
      val titleLc = title.toLowerCase
      val idx = titleLc.indexOf(y)
      if (idx == -1) { title } else { s"""${title.substring(0, idx)}[[${title.substring(idx, idx + y.length)}]]${title.substring(idx + y.length)}""" }
    }
    val html = replaced.replaceAllLiterally("[[", "<strong class=\"search-matched-text\">").replaceAllLiterally("]]", "</strong>")
    j.html(html)
  }
}
