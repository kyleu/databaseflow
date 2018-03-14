package models.scalaexport.thrift

import com.facebook.swift.parser.model.IntegerEnum
import services.scalaexport.db.ExportHelper

import scala.collection.JavaConverters._

case class ThriftIntegerEnum(e: IntegerEnum) {
  val name = ExportHelper.toClassName(e.getName)
  val fields = e.getFields.asScala.map(x => x.getName -> x.getValue)
}
