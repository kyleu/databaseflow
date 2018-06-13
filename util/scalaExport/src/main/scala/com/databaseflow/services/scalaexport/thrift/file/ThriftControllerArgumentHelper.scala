package com.databaseflow.services.scalaexport.thrift.file

import com.facebook.swift.parser.model._
import com.databaseflow.models.scalaexport.thrift.{ThriftMetadata, ThriftServiceMethod}

object ThriftControllerArgumentHelper {
  def defaultArgs(m: ThriftServiceMethod, metadata: ThriftMetadata) = {
    val argsMapped = m.arguments.map { arg =>
      s""""${arg.name}" -> ${getDefault(arg.t, metadata)}.asJson"""
    }.mkString(", ")
    s"Json.obj($argsMapped)"
  }

  private[this] def getDefault(t: ThriftType, metadata: ThriftMetadata): String = t match {
    case _: VoidType => "null"
    case i: IdentifierType => defaultForIdentifier(metadata.typedefs.getOrElse(i.getName, i.getName), metadata)
    case b: BaseType => defaultForBase(b.getType)
    case l: ListType =>
      val v = getDefault(l.getElementType, metadata)
      s"Seq($v)"
    case m: MapType =>
      val k = getDefault(m.getKeyType, metadata)
      val v = getDefault(m.getValueType, metadata)
      s"Map($k -> $v)"
    case s: SetType =>
      val v = getDefault(s.getElementType, metadata)
      s"Set($v)"
    case x => throw new IllegalStateException(s"Unhandled field type [$x]")
  }

  private[this] def defaultForIdentifier(name: String, metadata: ThriftMetadata): String = name match {
    case "I64" | "I32" => "0"
    case x if x.contains('.') => x.split('.').toList match {
      case _ :: cls :: Nil => cls + "()"
      case _ => throw new IllegalStateException(s"Cannot match [$x].")
    }
    case x if metadata.typedefs.contains(x) => defaultForIdentifier(metadata.typedefs(x), metadata)
    case x if metadata.enums.contains(x) => s"($x.${metadata.enums(x)}: $x)"
    case x => x + "()"
  }

  private[this] def defaultForBase(t: BaseType.Type) = t match {
    case BaseType.Type.BINARY => "null"
    case BaseType.Type.BOOL => "false"
    case BaseType.Type.BYTE => "0"
    case BaseType.Type.DOUBLE => "0.0"
    case BaseType.Type.I16 | BaseType.Type.I32 | BaseType.Type.I64 => "0"
    case BaseType.Type.STRING => "\"\""
    case x => throw new IllegalStateException(s"Unhandled base type [$x]")
  }
}
