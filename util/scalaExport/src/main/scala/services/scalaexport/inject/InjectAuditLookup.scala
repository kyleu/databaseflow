package services.scalaexport.inject

import better.files.File
import models.scalaexport.ExportResult
import models.schema.ColumnType

object InjectAuditLookup {
  def inject(result: ExportResult, rootDir: File) = {
    def serviceFieldsFor(s: String) = {
      val newContent = result.models.filterNot(_.provided).filter(_.pkFields.nonEmpty).map { model =>
        val svc = model.serviceReference.replaceAllLiterally("services.", "registry.")
        val args = model.pkFields.zipWithIndex.map { pkf =>
          val a = s"arg(${pkf._2})"
          pkf._1.t match {
            case ColumnType.StringType => a
            case ColumnType.IntegerType => s"intArg($a)"
            case ColumnType.LongType => s"longArg($a)"
            case ColumnType.ByteType => s"byteArg($a)"
            case ColumnType.FloatType => s"floatArg($a)"
            case ColumnType.TimestampType => s"ldtArg($a)"
            case ColumnType.DateType => s"ldArg($a)"
            case ColumnType.UuidType => s"uuidArg($a)"
            case x => throw new IllegalStateException(s"Invalid column type [$x].")
          }
        }.mkString(", ")
        val call = s"$svc.getByPrimaryKey($args)"

        s"""    case "${model.propertyName.toLowerCase}" => $call"""
      }.sorted.mkString("\n")

      InjectHelper.replaceBetween(original = s, start = "    /* Start registry lookups */", end = "    /* End registry lookups */", newContent = newContent)
    }

    val file = rootDir / "app" / "services" / "audit" / "AuditLookup.scala"
    val newContent = serviceFieldsFor(file.contentAsString)
    file.overwrite(newContent)

    "AuditLookup.scala" -> newContent
  }
}
