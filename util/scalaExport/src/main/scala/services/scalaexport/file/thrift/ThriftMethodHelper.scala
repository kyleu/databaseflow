package services.scalaexport.file.thrift

import models.scalaexport.thrift.{ThriftMetadata, ThriftStructField}

object ThriftMethodHelper {
  def getReturnMapping(t: String): String = t match {
    case "Unit" | "Boolean" | "String" | "Int" | "Long" | "Double" => ""
    case x if x.startsWith("Map[") => getReturnSubMapping(ThriftFieldHelper.mapKeyValFor(x)._2) match {
      case r if r.isEmpty => ".map(_.toMap)"
      case r => s".map(_.mapValues($r).toMap)"
    }
    case x if x.startsWith("Seq[") => getReturnSubMapping(t.drop(4).dropRight(1)) match {
      case r if r.isEmpty => ""
      case r => s".map(_.map($r))"
    }
    case x if x.startsWith("Set[") => getReturnSubMapping(t.drop(4).dropRight(1)) match {
      case r if r.isEmpty => ".map(_.toSet)"
      case r => s".map(_.map($r).toSet)"
    }
    case x => s".map($x.fromThrift)"
  }

  def getReturnSubMapping(t: String): String = t match {
    case "Unit" | "Boolean" | "String" | "Int" | "Long" | "Double" => ""
    case x if x.startsWith("Map[") => getReturnSubMapping(ThriftFieldHelper.mapKeyValFor(x)._2) match {
      case r if r.isEmpty => "_.toMap"
      case r => s"_.mapValues($r).toMap"
    }
    case x if x.startsWith("Seq[") => getReturnSubMapping(t.drop(4).dropRight(1)) match {
      case r if r.isEmpty => ""
      case r => s"_.map($r)"
    }
    case x if x.startsWith("Set[") => getReturnSubMapping(t.drop(4).dropRight(1)) match {
      case r if r.isEmpty => "_.toSet"
      case r => s"_.map($r).toSet"
    }
    case x => s"$x.fromThrift"
  }

  def getArgCall(field: ThriftStructField, metadata: ThriftMetadata) = {
    val (t, pkg) = ThriftFileHelper.columnTypeFor(field.t, metadata)
    parse(field.name, t, pkg, field.required)
  }

  private[this] def parse(name: String, t: String, pkg: Seq[String], required: Boolean): String = t match {
    case x if x.startsWith("Map[") =>
      val valuesMapped = parseMapped(ThriftFieldHelper.mapKeyValFor(x)._2, "map").replaceAllLiterally(".map", ".mapValues(")
      if (required) {
        s"$name$valuesMapped.toMap"
      } else {
        s"$name.map(_$valuesMapped).toMap"
      }
    case x if x.startsWith("Seq[") =>
      val remainder = t.drop(4).dropRight(1)
      val mapped = parseMapped(remainder, "seq")
      if (mapped.isEmpty) {
        s"$name"
      } else {
        s"$name.map(_$mapped)"
      }
    case x if x.startsWith("Set[") =>
      val remainder = t.drop(4).dropRight(1)
      val mapped = parseMapped(remainder, "set")
      if (mapped.isEmpty) {
        s"$name"
      } else {
        s"$name.map(_$mapped)"
      }
    case "Boolean" | "String" | "Int" | "Long" | "Double" => s"$name"
    case _ if required => s"$name.asThrift"
    case _ => s"$name.map(_.asThrift)"
  }

  private[this] def parseMapped(t: String, ctx: String): String = t match {
    case x if x.startsWith("Map[") => throw new IllegalStateException(s"Unhandled [$ctx] child Map")
    case x if x.startsWith("Seq[") => throw new IllegalStateException(s"Unhandled [$ctx] child Seq") // ""?
    case x if x.startsWith("Set[") => throw new IllegalStateException(s"Unhandled [$ctx] child Set")
    case "Boolean" | "String" | "Int" | "Long" | "Double" => ""
    case _ => s".asThrift"
  }
}
