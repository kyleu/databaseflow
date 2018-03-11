package services.scalaexport.file.thrift

import com.facebook.swift.parser.model._
import models.scalaexport.thrift.ThriftServiceMethod

object ThriftControllerArgumentHelper {
  def defaultArgs(m: ThriftServiceMethod, enums: Map[String, String], typedefs: Map[String, String]) = {
    val argsMapped = m.arguments.map { arg =>
      s""""${arg.name}" -> ${getDefault(arg.t, enums, typedefs)}.asJson"""
    }.mkString(", ")
    s"Json.obj($argsMapped)"
  }

  private[this] def getDefault(t: ThriftType, enums: Map[String, String], typedefs: Map[String, String]): String = t match {
    case _: VoidType => "null"
    case i: IdentifierType => defaultForIdentifier(typedefs.getOrElse(i.getName, i.getName), enums, typedefs)
    case b: BaseType => defaultForBase(b.getType)
    case l: ListType =>
      val v = getDefault(l.getElementType, enums, typedefs)
      s"Seq($v)"
    case m: MapType =>
      val k = getDefault(m.getKeyType, enums, typedefs)
      val v = getDefault(m.getValueType, enums, typedefs)
      s"Map($k -> $v)"
    case s: SetType =>
      val v = getDefault(s.getElementType, enums, typedefs)
      s"Set($v)"
    case x => throw new IllegalStateException(s"Unhandled field type [$x]")
  }

  private[this] def defaultForIdentifier(name: String, enums: Map[String, String], typedefs: Map[String, String]): String = name match {
    case "I64" | "I32" => "0"
    case x if x.contains('.') => x.split('.').toList match {
      case pkg :: cls :: Nil => cls + "()"
      case _ => throw new IllegalStateException(s"Cannot match [$x].")
    }
    case x if typedefs.contains(x) => defaultForIdentifier(typedefs(x), enums, typedefs)
    case x if enums.contains(x) => s"($x.${enums(x)}: $x)"
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
