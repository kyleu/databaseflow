package models.scalaexport.thrift

import com.facebook.swift.parser.model.ThriftMethod
import services.scalaexport.ExportHelper
import services.scalaexport.file.thrift.ThriftFileHelper

import scala.collection.JavaConverters._

case class ThriftServiceMethod(f: ThriftMethod) {
  val name = ExportHelper.toIdentifier(f.getName)
  val arguments = f.getArguments.asScala.map(ThriftStructField.apply)
  val returnValue = f.getReturnType

  def sig(typedefs: Map[String, String], pkgMap: Map[String, Seq[String]]) = {
    val retVal = ThriftFileHelper.columnTypeFor(returnValue, typedefs = typedefs, pkgMap)._1
    val argVals = arguments.map(arg => arg.name + ": " + ThriftFileHelper.columnTypeFor(arg.t, typedefs = typedefs, pkgMap)._1)
    s"$name(${argVals.mkString(", ")}): $retVal"
  }
}
