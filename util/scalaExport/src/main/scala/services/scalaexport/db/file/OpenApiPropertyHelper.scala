package services.scalaexport.db.file

import models.scalaexport.db.ExportField
import models.scalaexport.file.JsonFile
import models.schema.ColumnType

object OpenApiPropertyHelper {
  def contentFor(t: ColumnType, sqlTypeName: String, file: JsonFile) = t match {
    case ColumnType.UuidType =>
      file.add("\"type\": \"string\",")
      file.add("\"example\": \"00000000-0000-0000-0000-000000000000\"")
    case ColumnType.TimestampType =>
      file.add("\"type\": \"string\",")
      file.add("\"example\": \"2018-01-01 00:00:00\"")
    case ColumnType.TagsType | ColumnType.JsonType => file.add("\"type\": \"object\"")
    case ColumnType.ArrayType =>
      file.add("\"type\": \"array\",")
      sqlTypeName match {
        case x =>
          file.add("\"items\": {", 1)
          file.add("\"type\": \"" + x + "\"")
          file.add("}", -1)
      }
    case ColumnType.StringType => file.add("\"type\": \"string\"")
    case x => throw new IllegalStateException(s"Unhandled openapi property for type [$x].")
  }

  def propertyFor(f: ExportField, file: JsonFile, last: Boolean) = {
    val comma = if (last) { "" } else { "," }
    file.add("\"" + f.propertyName + "\": {", 1)
    contentFor(f.t, f.sqlTypeName, file)
    file.add("}" + comma, -1)
  }
}
