package services.scalaexport.file.thrift

import models.scalaexport.thrift.ThriftStructField

object ThriftFieldScalaHelper {
  def getValFor(field: ThriftStructField, typedefs: Map[String, String], pkgMap: Map[String, Seq[String]]) = {
    val (t, pkg) = ThriftFileHelper.columnTypeFor(field.t, typedefs = typedefs, pkgMap)
    parse("t", field.name, t, pkg, field.required)
  }

  private[this] def parse(root: String, name: String, t: String, pkg: Seq[String], required: Boolean): String = t match {
    case x if x.startsWith("Map[") =>
      val (_, v) = t.drop(4).dropRight(1).split(',').map(_.trim).toList match {
        case key :: value :: Nil => key -> value
        case other => throw new IllegalStateException(s"TODO: $other")
      }
      val valuesMapped = parseMapped(v, "map").replaceAllLiterally(".map", ".mapValues(")
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
    case x if x.startsWith("Seq[") => throw new IllegalStateException(s"Unhandled [$ctx] child Seq")
    case x if x.startsWith("Set[") => throw new IllegalStateException(s"Unhandled [$ctx] child Set")
    case "Boolean" | "String" | "Int" | "Long" | "Double" => ""
    case x => s".map($x.fromThrift)"
  }

}
