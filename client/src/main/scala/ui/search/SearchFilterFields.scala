package ui.search

import java.util.UUID

import ui.metadata.MetadataManager
import ui.query.SavedQueryManager

trait SearchFilterFields {
  protected[this] def savedQueryFields(id: String) = SavedQueryManager.savedQueries.get(UUID.fromString(id)) match {
    case Some(sq) => Seq("id" -> id, "name" -> sq.name, "description" -> sq.description.getOrElse(""), "sql" -> sq.sql)
    case None => Seq("id" -> id)
  }
  protected[this] def tableFields(name: String) = MetadataManager.schema.flatMap(_.tables.find(_.name == name)) match {
    case Some(t) => Seq("name" -> name, "description" -> t.description.getOrElse("")) ++ t.columns.map(c => "column" -> c.name)
    case None => Seq("name" -> name)
  }
  protected[this] def viewFields(name: String) = MetadataManager.schema.flatMap(_.views.find(_.name == name)) match {
    case Some(v) => Seq("name" -> name, "description" -> v.description.getOrElse("")) ++ v.columns.map(c => "column" -> c.name)
    case None => Seq("name" -> name)
  }
  protected[this] def procedureFields(name: String) = MetadataManager.schema.flatMap(_.procedures.find(_.name == name)) match {
    case Some(p) => Seq("name" -> name, "description" -> p.description.getOrElse("")) ++ p.params.map(p => "parameter" -> p.name)
    case None => Seq("name" -> name)
  }
}
