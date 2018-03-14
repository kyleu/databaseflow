package models.scalaexport.thrift

import com.facebook.swift.parser.model.ThriftField
import com.facebook.swift.parser.model.ThriftField.Requiredness
import services.scalaexport.db.ExportHelper

object ThriftStructField {
  protected val renames = Map("type" -> "`type`")
}

case class ThriftStructField(f: ThriftField) {
  val originalName = f.getName
  val id = ExportHelper.toIdentifier(originalName)
  val name = ThriftStructField.renames.getOrElse(id, id)
  val required = f.getRequiredness != Requiredness.OPTIONAL
  val t = f.getType
  val value = Option(f.getValue.orNull)
}
