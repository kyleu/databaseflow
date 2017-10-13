package services.scalaexport.inject

import better.files.File
import models.scalaexport.ExportResult

object InjectAuditLookup {
  def inject(result: ExportResult, rootDir: File) = {
    def serviceFieldsFor(s: String) = {
      val newContent = result.models.filterNot(_.provided).filter(_.pkFields.nonEmpty).map { model =>
        val svc = model.serviceReference.replaceAllLiterally("services.", "registry.")
        s"""    case "${model.propertyName.toLowerCase}" => $svc.getByPrimaryKey(${model.pkArgs})"""
      }.sorted.mkString("\n")

      InjectHelper.replaceBetween(original = s, start = "    /* Start registry lookups */", end = "    /* End registry lookups */", newContent = newContent)
    }

    val file = rootDir / "app" / "services" / "audit" / "AuditLookup.scala"
    val newContent = serviceFieldsFor(file.contentAsString)
    file.overwrite(newContent)

    "AuditLookup.scala" -> newContent
  }
}
