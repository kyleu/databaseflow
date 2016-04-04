package services.codegen

import models.codegen.Engine
import models.codegen.Engine._

object SqlProvider {
  def varchar(implicit engine: Engine) = engine match {
    case PostgreSQL => "character varying"
    case _ => "varchar"
  }

  def quoteIdentifier(implicit engine: Engine) = engine match {
    case PostgreSQL | H2 => "\\\""
    case MySQL => "`"
    case _ => "varchar"
  }

  def showCreateTableSupported(implicit engine: Engine) = engine match {
    case MySQL => true
    case _ => false
  }

  def showCreateTable(implicit engine: Engine) = engine match {
    case MySQL => "\"show create table \" + tableName"
    case _ => false
  }
}
