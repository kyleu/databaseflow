package com.databaseflow.models.scalaexport.thrift

case class ThriftMetadata(
    typedefs: Map[String, String],
    enums: Map[String, String],
    pkgMap: Map[String, Seq[String]]
)
