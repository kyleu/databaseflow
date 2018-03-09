package models.scalaexport.thrift

import com.facebook.swift.parser.model.ThriftField
import com.facebook.swift.parser.model.ThriftField.Requiredness

case class ThriftStructField(f: ThriftField) {
  val name = f.getName
  val required = f.getRequiredness != Requiredness.OPTIONAL
  val t = f.getType
  val value = Option(f.getValue.orNull)
}
