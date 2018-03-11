package services.scalaexport.file.thrift

import com.facebook.swift.parser.model._
import models.scalaexport.thrift.ThriftMetadata
import models.schema.ColumnType

object ThriftFileHelper {
  def columnTypeFor(t: ThriftType, metadata: ThriftMetadata): (String, Seq[String]) = t match {
    case _: VoidType => "Unit" -> Nil
    case i: IdentifierType => colTypeForIdentifier(metadata.typedefs.getOrElse(i.getName, i.getName), metadata)
    case b: BaseType => colTypeForBase(b.getType) -> Nil
    case l: ListType =>
      val v = columnTypeFor(l.getElementType, metadata)
      s"Seq[${v._1}]" -> v._2
    case m: MapType =>
      val k = columnTypeFor(m.getKeyType, metadata)
      val v = columnTypeFor(m.getValueType, metadata)
      s"Map[${k._1}, ${v._1}]" -> (k._2 ++ v._2)
    case s: SetType =>
      val v = columnTypeFor(s.getElementType, metadata)
      s"Set[${v._1}]" -> v._2
    case x => throw new IllegalStateException(s"Unhandled field type [$x]")
  }

  def declarationFor(
    required: Boolean,
    name: String,
    value: Option[ConstValue],
    metadata: ThriftMetadata,
    colType: String
  ) = {
    val propType = if (required) { colType } else { "Option[" + colType + "]" }
    s"$name: $propType${propDefault(colType, required, value.map(_.value), metadata.enums)}"
  }

  private[this] def defaultForType(colType: String, enums: Map[String, String]) = colType match {
    case x if x.startsWith("Seq[") => "Nil"
    case x if x.startsWith("Set[") => "Set.empty"
    case x if x.startsWith("Map[") => "Map.empty"
    case x if x.startsWith("Option[") => "None"
    case "Boolean" => "false"
    case "String" => "\"\""
    case "Int" => "0"
    case "Long" => "0L"
    case "Double" => "0.0"
    case x if enums.contains(x) => x + "." + enums(x)
    case x => x + "()"
  }

  private[this] def propDefault(colType: String, required: Boolean, value: Option[Any], enums: Map[String, String]) = value match {
    case Some(v) if required => " = " + v
    case Some(v) => " = Some(" + v + ")"
    case None if required => " = " + defaultForType(colType, enums)
    case None => " = None"
  }

  private[this] def colTypeForIdentifier(name: String, metadata: ThriftMetadata): (String, Seq[String]) = name match {
    case "I64" => ColumnType.LongType.asScala -> Nil
    case "I32" => ColumnType.IntegerType.asScala -> Nil
    case x if x.contains('.') => x.split('.').toList match {
      case pkg :: cls :: Nil => cls -> metadata.pkgMap(pkg)
      case _ => throw new IllegalStateException(s"Cannot match [$x].")
    }
    case x => metadata.typedefs.get(x).map(td => colTypeForIdentifier(td, metadata)._1).getOrElse(x) -> Nil
  }

  private[this] def colTypeForBase(t: BaseType.Type) = t match {
    case BaseType.Type.BINARY => ColumnType.ByteArrayType.asScala
    case BaseType.Type.BOOL => ColumnType.BooleanType.asScala
    case BaseType.Type.BYTE => ColumnType.ByteType.asScala
    case BaseType.Type.DOUBLE => ColumnType.DoubleType.asScala
    case BaseType.Type.I16 | BaseType.Type.I32 => ColumnType.IntegerType.asScala
    case BaseType.Type.I64 => ColumnType.LongType.asScala
    case BaseType.Type.STRING => ColumnType.StringType.asScala
    case x => throw new IllegalStateException(s"Unhandled base type [$x]")
  }
}
