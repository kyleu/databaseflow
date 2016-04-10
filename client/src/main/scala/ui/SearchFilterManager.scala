package ui

import java.util.UUID

import org.scalajs.jquery.{ JQuery, jQuery => $ }

object SearchFilterManager {
  private[this] lazy val mainMenu = $("#main-menu")

  private[this] def savedQueryFields(id: String) = SavedQueryManager.savedQueries.get(UUID.fromString(id)) match {
    case Some(sq) => Seq("id" -> id, "name" -> sq.name, "description" -> sq.description.getOrElse(""), "sql" -> sq.sql)
    case None => Seq("id" -> id)
  }
  private[this] def tableFields(name: String) = MetadataManager.schema.flatMap(_.tables.find(_.name == name)) match {
    case Some(t) => Seq("name" -> name, "description" -> t.description.getOrElse("")) ++ t.columns.map(c => "column" -> c.name)
    case None => Seq("name" -> name)
  }
  private[this] def viewFields(name: String) = MetadataManager.schema.flatMap(_.views.find(_.name == name)) match {
    case Some(v) => Seq("name" -> name, "description" -> v.description.getOrElse("")) ++ v.columns.map(c => "column" -> c.name)
    case None => Seq("name" -> name)
  }
  private[this] def procedureFields(name: String) = MetadataManager.schema.flatMap(_.procedures.find(_.name == name)) match {
    case Some(p) => Seq("name" -> name, "description" -> p.description.getOrElse("")) ++ p.params.map(p => "parameter" -> p.name)
    case None => Seq("name" -> name)
  }

  def filterSchema(searches: Seq[String]) = {
    MetadataManager.savedQueries.foreach { savedQueries =>
      filterObjects("saved-query", savedQueries, searches, SearchManager.savedQueriesToggle, savedQueryFields)
    }
    MetadataUpdates.tables.foreach { tables =>
      filterObjects("table", tables, searches, SearchManager.tablesToggle, tableFields)
    }
    MetadataUpdates.views.foreach { views =>
      filterObjects("view", views, searches, SearchManager.viewsToggle, viewFields)
    }
    MetadataUpdates.procedures.foreach { procedures =>
      filterObjects("procedure", procedures, searches, SearchManager.proceduresToggle, procedureFields)
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

  private[this] def highlightAdditionalMatches(keys: Seq[(String, String)], matches: Seq[String], j: JQuery) = {
    val els = keys.map { key =>
      val replaced = matches.foldLeft(key._2) { (x, y) =>
        val keyLc = key._2.toLowerCase
        val idx = keyLc.indexOf(y)
        if (idx == -1) { key._2 } else { s"""${key._2.substring(0, idx)}[${key._2.substring(idx, idx + y.length)}]${key._2.substring(idx + y.length)}""" }
      }
      s"""Matches ${key._1} "$replaced"."""
    }
    j.attr("title", els.mkString("\n"))
  }

  private[this] def filterObjects(
    key: String, seq: Seq[(String, JQuery, JQuery)], searches: Seq[String], toggle: JQuery, searchF: (String) => Seq[(String, String)]
  ) = {
    val matches = seq.map { t =>
      val values = searchF(t._1)
      val results = values.flatMap(v => matchKey(searches, v))
      t -> results
    }

    val (matched, notMatched) = matches.partition(_._2.nonEmpty)
    matched.foreach { o =>
      o._2.find(_._1 == "name") match {
        case Some(name) => highlightTitleMatches(name._2, searches, o._1._2, o._1._3)
        case None => highlightAdditionalMatches(o._2, searches, o._1._2)
      }
      o._1._2.show()
    }
    notMatched.foreach(_._1._2.hide())
    if (matched.isEmpty) { SearchManager.closeIfOpen(toggle) } else { SearchManager.openIfClosed(toggle) }
    //utils.Logging.info(s"Matched [${matched.size}] and skipped [${notMatched.size}] ${key}s.")
  }

  private[this] def matchKey(searches: Seq[String], key: (String, String)) = {
    val lcn = key._2.toLowerCase
    if (searches.forall(s => lcn.contains(s))) {
      Some(key)
    } else {
      None
    }
  }
}
