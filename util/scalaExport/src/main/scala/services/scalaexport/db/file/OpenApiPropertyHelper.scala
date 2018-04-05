package services.scalaexport.db.file

import models.scalaexport.db.{ExportEnum, ExportField}
import models.scalaexport.file.JsonFile
import models.schema.ColumnType

object OpenApiPropertyHelper {
  def contentFor(t: ColumnType, sqlTypeName: String, file: JsonFile, enums: Seq[ExportEnum]) = t match {
    case ColumnType.IntegerType | ColumnType.LongType => file.add("\"type\": \"integer\"")
    case ColumnType.BigDecimalType | ColumnType.DoubleType | ColumnType.FloatType => file.add("\"type\": \"number\"")
    case ColumnType.UuidType =>
      file.add("\"type\": \"string\",")
      file.add("\"example\": \"00000000-0000-0000-0000-000000000000\"")
    case ColumnType.BooleanType => file.add("\"type\": \"boolean\"")
    case ColumnType.TimestampType =>
      file.add("\"type\": \"string\",")
      file.add("\"example\": \"2018-01-01 00:00:00\"")
    case ColumnType.TimeType =>
      file.add("\"type\": \"string\",")
      file.add("\"example\": \"00:00:00\"")
    case ColumnType.DateType =>
      file.add("\"type\": \"string\",")
      file.add("\"example\": \"2018-01-01\"")
    case ColumnType.TagsType | ColumnType.JsonType => file.add("\"type\": \"object\"")
    case ColumnType.EnumType =>
      val e = enums.find(_.name == sqlTypeName).getOrElse(throw new IllegalStateException(s"Cannot file enum [$sqlTypeName]."))
      file.add("\"type\": \"string\"")
    case ColumnType.ArrayType =>
      file.add("\"type\": \"array\",")
      sqlTypeName match {
        case x =>
          file.add("\"items\": {", 1)
          file.add("\"type\": \"" + x + "\"")
          file.add("}", -1)
      }
    case ColumnType.StringType | ColumnType.EncryptedStringType | ColumnType.UnknownType => file.add("\"type\": \"string\"")
    case ColumnType.ByteArrayType => file.add("\"type\": \"string\"")
    case x => throw new IllegalStateException(s"Unhandled openapi property for type [$x].")
  }

  def propertyFor(f: ExportField, file: JsonFile, last: Boolean, enums: Seq[ExportEnum]) = {
    val comma = if (last) { "" } else { "," }
    file.add("\"" + f.propertyName + "\": {", 1)
    contentFor(f.t, f.sqlTypeName, file, enums)
    file.add("}" + comma, -1)
  }
}
