package models.scalaexport.thrift

import com.facebook.swift.parser.model.Service
import services.scalaexport.ExportHelper

import scala.collection.JavaConverters._

case class ThriftService(s: Service) {
  val name = ExportHelper.toClassName(s.getName)
  val identifier = ExportHelper.toIdentifier(name)
  val methods = s.getMethods.asScala.map(ThriftServiceMethod.apply)
}
