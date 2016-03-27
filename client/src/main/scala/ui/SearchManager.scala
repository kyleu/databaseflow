package ui

import models.query.SavedQuery
import models.schema.{ Procedure, Table }
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

  private[this] def clearSearch() = {
    $(".saved-query-link, .table-link, .view-link, .procedure-link").removeClass("search-ignored")
    closeIfOpen(savedQueriesToggle)
    closeIfOpen(tablesToggle)
    closeIfOpen(viewsToggle)
    closeIfOpen(proceduresToggle)
    MetadataManager.savedQueries.foreach(_.foreach { x =>
      x._3.text(x._1.title)
      x._2.show()
    })
    MetadataManager.tables.foreach(_.foreach { x =>
      x._3.text(x._1.name)
      x._2.show()
    })
    MetadataManager.views.foreach(_.foreach { x =>
      x._3.text(x._1.name)
      x._2.show()
    })
    MetadataManager.procedures.foreach(_.foreach { x =>
      x._3.text(x._1.name)
      x._2.show()
    })
  }

  private[this] def highlightMatches(title: String, matches: Seq[String], j: JQuery) = {
    val replaced = matches.foldLeft(title) { (x, y) =>
      val titleLc = title.toLowerCase
      val idx = titleLc.indexOf(y)
      if (idx == -1) {
        title
      } else {
        val pre = title.substring(0, idx)
        val chunk = title.substring(idx, idx + y.length)
        val post = title.substring(idx + y.length)
        s"""$pre[[$y]]$post"""
      }
    }
    val html = replaced.replaceAllLiterally("[[", "<strong class=\"search-matched-text\">").replaceAllLiterally("]]", "</strong>")
    j.html(html)
  }

  private[this] def filterObjects[T](
    key: String, seq: Seq[(T, JQuery, JQuery)], searches: Seq[String],
    matchFunc: (Seq[String], T) => Boolean, titleFunc: (T) => String, toggle: JQuery
  ) = {
    val (matched, notMatched) = seq.partition { t =>
      matchFunc(searches, t._1)
    }
    matched.foreach { o =>
      highlightMatches(titleFunc(o._1), searches, o._3)
      o._2.show()
    }
    notMatched.foreach { o =>
      o._2.hide()
    }
    if (matched.isEmpty) {
      closeIfOpen(toggle)
    } else {
      openIfClosed(toggle)
    }
    Logging.info(s"Matched [${matched.size}] and skipped [${notMatched.size}] ${key}s.")
  }

  private[this] def matchSavedQuery(searches: Seq[String], sq: SavedQuery) = {
    val lct = sq.title.toLowerCase
    searches.forall(s => lct.contains(s))
  }
  private[this] def matchTable(searches: Seq[String], t: Table) = {
    val lcn = t.name.toLowerCase
    searches.forall(s => lcn.contains(s))
  }
  private[this] def matchProcedure(searches: Seq[String], p: Procedure) = {
    val lcn = p.name.toLowerCase
    searches.forall(s => lcn.contains(s))
  }

  private[this] def filterSchema(searches: Seq[String]) = {
    MetadataManager.savedQueries.foreach { savedQueries =>
      filterObjects("saved-query", savedQueries, searches, matchSavedQuery, (x: SavedQuery) => x.title, savedQueriesToggle)
    }
    MetadataManager.tables.map { tables =>
      filterObjects("table", tables, searches, matchTable, (x: Table) => x.name, tablesToggle)
    }
    MetadataManager.views.map { views =>
      filterObjects("view", views, searches, matchTable, (x: Table) => x.name, viewsToggle)
    }
    MetadataManager.procedures.map { procedures =>
      filterObjects("procedure", procedures, searches, matchProcedure, (x: Procedure) => x.name, proceduresToggle)
    }
  }
}
