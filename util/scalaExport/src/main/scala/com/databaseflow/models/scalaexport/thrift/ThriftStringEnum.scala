package com.databaseflow.models.scalaexport.thrift

import com.facebook.swift.parser.model.StringEnum
import com.databaseflow.services.scalaexport.ExportHelper

import scala.collection.JavaConverters._

case class ThriftStringEnum(e: StringEnum) {
  val name = ExportHelper.toClassName(e.getName)
  val values = e.getValues.asScala
}
