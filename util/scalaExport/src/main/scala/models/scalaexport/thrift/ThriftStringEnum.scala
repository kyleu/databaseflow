package models.scalaexport.thrift

import com.facebook.swift.parser.model.StringEnum

import scala.collection.JavaConverters._

case class ThriftStringEnum(e: StringEnum) {
  val name = e.getName
  val values = e.getValues.asScala
}
