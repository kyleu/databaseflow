package com.databaseflow.models.scalaexport.thrift

import com.facebook.swift.parser.model.IntegerEnum
import com.databaseflow.services.scalaexport.ExportHelper

import scala.collection.JavaConverters._

case class ThriftIntegerEnum(e: IntegerEnum) {
  val name = ExportHelper.toClassName(e.getName)
  val fields = e.getFields.asScala.map(x => x.getName -> x.getValue)
}
