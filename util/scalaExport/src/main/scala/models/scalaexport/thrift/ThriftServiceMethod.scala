package models.scalaexport.thrift

import com.facebook.swift.parser.model.ThriftMethod

import scala.collection.JavaConverters._

case class ThriftServiceMethod(f: ThriftMethod) {
  val arguments = f.getArguments.asScala.map(ThriftStructField.apply)

  val name = f.getName
}
