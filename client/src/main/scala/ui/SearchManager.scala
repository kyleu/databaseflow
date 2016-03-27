package ui

import org.scalajs.jquery.{ JQuery, JQueryEventObject, jQuery => $ }
import utils.Logging

object SearchManager {
  private[this] lazy val searchContainer = $(".search-wrapper")
  private[this] lazy val searchInput = $("input#search", searchContainer)
  private[this] lazy val searchIcon = $(".fa", searchContainer)

  private[this] lazy val savedQueriesToggle = $("#saved-query-list-toggle")
  private[this] lazy val tablesToggle = $("#table-list-toggle")
  private[this] lazy val viewsToggle = $("#view-list-toggle")
  private[this] lazy val proceduresToggle = $("#procedure-list-toggle")

  private[this] var currentSearch = ""

  def init() = {
    if (searchContainer.length != 1 || searchInput.length != 1) {
      throw new IllegalStateException("Missing search input field.")
    }

    searchInput.focus { (e: JQueryEventObject) =>
      searchInput.parent().addClass("focused")
    }
    searchInput.blur { (e: JQueryEventObject) =>
      searchInput.parent().removeClass("focused")
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
      if (searchIcon.hasClass("fa-close")) {
        searchIcon.removeClass("fa-close").addClass("fa-search").css("pointer", "default")
      }
      clearSearch()
    } else {
      Logging.info(s"Searching [$search]...")
      if (searchIcon.hasClass("fa-search")) {
        searchIcon.removeClass("fa-search").addClass("fa-close").css("pointer", "cursor")
      }
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

  private[this] def clearSearchEntries(o: Option[scala.Seq[(String, JQuery, JQuery)]], toggle: JQuery) = {
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

  private[this] def filterObjects(key: String, seq: Seq[(String, JQuery, JQuery)], searches: Seq[String], toggle: JQuery) = {
    val (matched, notMatched) = seq.partition(t => matchName(searches, t._1))
    matched.foreach { o =>
      highlightMatches(o._1, searches, o._3)
      o._2.show()
    }
    notMatched.foreach(_._2.hide())
    if (matched.isEmpty) { closeIfOpen(toggle) } else { openIfClosed(toggle) }
    Logging.info(s"Matched [${matched.size}] and skipped [${notMatched.size}] ${key}s.")
  }

  private[this] def matchName(searches: Seq[String], name: String) = {
    val lcn = name.toLowerCase
    searches.forall(s => lcn.contains(s))
  }

  private[this] def filterSchema(searches: Seq[String]) = {
    MetadataManager.savedQueries.foreach { savedQueries =>
      filterObjects("saved-query", savedQueries.map(x => (x._1.id.toString, x._2, x._3)), searches, savedQueriesToggle)
    }
    MetadataManager.tables.foreach(tables => filterObjects("table", tables, searches, tablesToggle))
    MetadataManager.views.foreach(views => filterObjects("view", views, searches, viewsToggle))
    MetadataManager.procedures.foreach(procedures => filterObjects("procedure", procedures, searches, proceduresToggle))
  }
}
