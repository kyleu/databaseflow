package ui

import org.scalajs.jquery.{ JQuery, jQuery => $ }
import utils.Logging

object SearchFilterManager {
  def filterSchema(searches: Seq[String]) = {
    MetadataManager.savedQueries.foreach { savedQueries =>
      filterObjects("saved-query", savedQueries.map(x => (x._1.id.toString, x._2, x._3)), searches, SearchManager.savedQueriesToggle)
    }
    MetadataManager.tables.foreach(tables => filterObjects("table", tables, searches, SearchManager.tablesToggle))
    MetadataManager.views.foreach(views => filterObjects("view", views, searches, SearchManager.viewsToggle))
    MetadataManager.procedures.foreach(procedures => filterObjects("procedure", procedures, searches, SearchManager.proceduresToggle))
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
    if (matched.isEmpty) { SearchManager.closeIfOpen(toggle) } else { SearchManager.openIfClosed(toggle) }
    Logging.info(s"Matched [${matched.size}] and skipped [${notMatched.size}] ${key}s.")
  }

  private[this] def matchName(searches: Seq[String], name: String) = {
    val lcn = name.toLowerCase
    searches.forall(s => lcn.contains(s))
  }
}
