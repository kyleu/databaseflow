package services.scalaexport.file.thrift

import models.scalaexport.thrift.ThriftStructField

object ThriftFieldThriftHelper {
  def getValFor(field: ThriftStructField, typedefs: Map[String, String], pkgMap: Map[String, Seq[String]]) = {
    val (t, pkg) = ThriftFileHelper.columnTypeFor(field.t, typedefs = typedefs, pkgMap)
    parse(field.name, t, pkg, field.required)
  }

  private[this] def parse(name: String, t: String, pkg: Seq[String], required: Boolean): String = t match {
    case x if x.startsWith("Map[") =>
      val valuesMapped = parseMapped(ThriftFieldHelper.mapKeyValFor(x)._2, "map").replaceAllLiterally(".map", ".mapValues(")
      if (required) {
        s"$name$valuesMapped.toMap"
      } else {
        s"$name.map(x => x$valuesMapped).toMap"
      }
    case x if x.startsWith("Seq[") =>
      val remainder = t.drop(4).dropRight(1)
      val mapped = parseMapped(remainder, "seq")
      if (required) {
        s"$name$mapped"
      } else {
        s"$name.map(x => x$mapped)"
      }
    case x if x.startsWith("Set[") =>
      val remainder = t.drop(4).dropRight(1)
      val mapped = parseMapped(remainder, "set")
      if (required) {
        s"$name$mapped.toSet"
      } else {
        s"$name.map(x => x$mapped.toSet)"
      }
    case "Boolean" | "String" | "Int" | "Long" | "Double" => s"$name"
    case _ if required => s"$name.asThrift"
    case _ => s"$name.map(_.asThrift)"
  }

  private[this] def parseMapped(t: String, ctx: String): String = t match {
    case x if x.startsWith("Map[") => throw new IllegalStateException(s"Unhandled [$ctx] child Map")
    case x if x.startsWith("Seq[") => "" // throw new IllegalStateException(s"Unhandled [$ctx] child Seq")
    case x if x.startsWith("Set[") => throw new IllegalStateException(s"Unhandled [$ctx] child Set")
    case "Boolean" | "String" | "Int" | "Long" | "Double" => ""
    case _ => s".map(_.asThrift)"
  }
}
