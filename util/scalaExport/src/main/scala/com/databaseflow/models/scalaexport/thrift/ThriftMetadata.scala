package com.databaseflow.models.scalaexport.thrift

case class ThriftMetadata(
    depPrefix: String,
    typedefs: Map[String, String],
    enums: Map[String, String],
    pkgMap: Map[String, Seq[String]]
)
