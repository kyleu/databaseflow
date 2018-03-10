package models.scalaexport.thrift

import com.facebook.swift.parser.model.ThriftMethod
import services.scalaexport.ExportHelper

import scala.collection.JavaConverters._

case class ThriftServiceMethod(f: ThriftMethod) {
  val name = ExportHelper.toIdentifier(f.getName)
  val arguments = f.getArguments.asScala.map(ThriftStructField.apply)
  val returnValue = f.getReturnType
}
