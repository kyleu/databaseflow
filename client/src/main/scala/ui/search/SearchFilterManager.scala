package ui.search

import org.scalajs.jquery.JQuery
import ui.metadata.{MetadataManager, ProcedureUpdates, TableUpdates, ViewUpdates}

object SearchFilterManager extends SearchFilterFields {
  def filterSchema(searches: Seq[String]) = {
    MetadataManager.savedQueries.foreach { savedQueries =>
      filterObjects(savedQueries, searches, SearchManager.savedQueriesToggle, savedQueryFields)
    }
    TableUpdates.tables.foreach { tables =>
      filterObjects(tables, searches, SearchManager.tablesToggle, tableFields)
    }
    ViewUpdates.views.foreach { views =>
      filterObjects(views, searches, SearchManager.viewsToggle, viewFields)
    }
    ProcedureUpdates.procedures.foreach { procedures =>
      filterObjects(procedures, searches, SearchManager.proceduresToggle, procedureFields)
    }
  }

  private[this] def highlightTitleMatches(title: String, matches: Seq[String], anchorEl: JQuery, titleEl: JQuery) = {
    val replaced = matches.foldLeft(title) { (x, y) =>
      val titleLc = title.toLowerCase
      val idx = titleLc.indexOf(y)
      if (idx == -1) { title } else { s"""${title.substring(0, idx)}[[${title.substring(idx, idx + y.length)}]]${title.substring(idx + y.length)}""" }
    }
    val html = replaced.replaceAllLiterally("[[", "<strong class=\"search-matched-text\">").replaceAllLiterally("]]", "</strong>")
    anchorEl.attr("title", replaced.replaceAllLiterally("[[", "[").replaceAllLiterally("]]", "]"))
    titleEl.html(html)
  }

  private[this] def highlightAdditionalMatches(keys: Seq[(String, String)], matches: Seq[String], anchorEl: JQuery, titleEl: JQuery) = {
    val els = keys.map { key =>
      val replaced = matches.foldLeft(key._2) { (x, y) =>
        val keyLc = key._2.toLowerCase
        val idx = keyLc.indexOf(y)
        if (idx == -1) { key._2 } else { s"""${key._2.substring(0, idx)}[${key._2.substring(idx, idx + y.length)}]${key._2.substring(idx + y.length)}""" }
      }
      s"""Matches ${key._1} "$replaced"."""
    }
    val name = anchorEl.data("name").toString
    titleEl.text(name)
    anchorEl.attr("title", els.mkString("\n"))
  }

  private[this] def filterObjects(seq: Seq[(String, JQuery, JQuery)], searches: Seq[String], toggle: JQuery, searchF: (String) => Seq[(String, String)]) = {
    val matches = seq.map { t =>
      val values = searchF(t._1)
      val results = values.flatMap(v => matchKey(searches, v))
      t -> results
    }

    val (matched, notMatched) = matches.partition(_._2.nonEmpty)
    val searchValues = searches.map(_.split(":").last)
    matched.foreach { o =>
      o._2.find(_._1 == "name") match {
        case Some(name) => highlightTitleMatches(name._2, searchValues, o._1._2, o._1._3)
        case None => highlightAdditionalMatches(o._2, searchValues, o._1._2, o._1._3)
      }
      o._1._2.show()
    }
    notMatched.foreach(_._1._2.hide())
    if (matched.isEmpty) { SearchManager.closeIfOpen(toggle) } else { SearchManager.openIfClosed(toggle) }
    //utils.Logging.info(s"Matched [${matched.size}] and skipped [${notMatched.size}] ${key}s.")
  }

  private[this] def matchKey(searches: Seq[String], key: (String, String)) = {
    val lcn = key._2.toLowerCase
    val matches = searches.forall { s =>
      if (s.contains(':')) {
        val split = s.split(':')
        key._1 == split(0) && lcn.contains(split(1))
      } else {
        lcn.contains(s)
      }
    }
    if (matches) { Some(key) } else { None }
  }
}
