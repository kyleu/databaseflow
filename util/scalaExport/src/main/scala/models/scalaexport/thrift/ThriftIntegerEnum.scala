package models.scalaexport.thrift

import com.facebook.swift.parser.model.IntegerEnum

import scala.collection.JavaConverters._

case class ThriftIntegerEnum(e: IntegerEnum) {
  val name = e.getName
  val fields = e.getFields.asScala.map(x => x.getName -> x.getValue)
}
