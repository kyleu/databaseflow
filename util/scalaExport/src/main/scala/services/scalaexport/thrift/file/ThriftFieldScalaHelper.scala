package services.scalaexport.thrift.file

import models.scalaexport.thrift.{ThriftMetadata, ThriftStructField}

object ThriftFieldScalaHelper {
  def getFromThrift(field: ThriftStructField, metadata: ThriftMetadata) = {
    val (t, pkg) = ThriftFileHelper.columnTypeFor(field.t, metadata)
    parse("t", field.name, t, pkg, field.required || field.value.isDefined)
  }

  private[this] def parse(root: String, name: String, t: String, pkg: Seq[String], required: Boolean): String = t match {
    case x if x.startsWith("Map[") =>
      val valuesMapped = parseMapped(ThriftFieldHelper.mapKeyValFor(x)._2, "map").replaceAllLiterally(".map", ".mapValues(")
      if (required) {
        s"$root.$name$valuesMapped.toMap"
      } else {
        s"$root.$name.map(x => x$valuesMapped).toMap"
      }
    case x if x.startsWith("Seq[") =>
      val remainder = t.drop(4).dropRight(1)
      val mapped = parseMapped(remainder, "seq")
      if (required) {
        s"$root.$name$mapped"
      } else {
        s"$root.$name.map(x => x$mapped)"
      }
    case x if x.startsWith("Set[") =>
      val remainder = t.drop(4).dropRight(1)
      val mapped = parseMapped(remainder, "set")
      if (required) {
        s"$root.$name$mapped.toSet"
      } else {
        s"$root.$name.map(x => x$mapped.toSet)"
      }
    case "Boolean" | "String" | "Int" | "Long" | "Double" => s"$root.$name"
    case x if required => s"$x.fromThrift($root.$name)"
    case x => s"$root.$name.map($x.fromThrift)"
  }

  private[this] def parseMapped(t: String, ctx: String): String = t match {
    case x if x.startsWith("Map[") => throw new IllegalStateException(s"Unhandled [$ctx] child Map")
    case x if x.startsWith("Seq[") => "" // throw new IllegalStateException(s"Unhandled [$ctx] child Seq")
    case x if x.startsWith("Set[") => throw new IllegalStateException(s"Unhandled [$ctx] child Set")
    case "Boolean" | "String" | "Int" | "Long" | "Double" => ""
    case x => s".map($x.fromThrift)"
  }
}
