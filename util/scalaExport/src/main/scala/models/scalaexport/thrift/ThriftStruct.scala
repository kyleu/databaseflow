package models.scalaexport.thrift

import com.facebook.swift.parser.model.Struct

import scala.collection.JavaConverters._

case class ThriftStruct(s: Struct) {
  val name = s.getName
  val fields = s.getFields.asScala.map(ThriftStructField.apply)
}
