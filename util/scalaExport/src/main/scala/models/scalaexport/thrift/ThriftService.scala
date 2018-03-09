package models.scalaexport.thrift

import com.facebook.swift.parser.model.Service

import scala.collection.JavaConverters._

case class ThriftService(s: Service) {
  val name = s.getName
  val methods = s.getMethods.asScala.map(ThriftServiceMethod.apply)
}
