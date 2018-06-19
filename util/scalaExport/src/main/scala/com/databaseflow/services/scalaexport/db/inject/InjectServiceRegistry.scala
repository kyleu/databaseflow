package com.databaseflow.services.scalaexport.db.inject

import better.files.File
import com.databaseflow.models.scalaexport.db.ExportResult
import com.databaseflow.services.scalaexport.ExportHelper

object InjectServiceRegistry {
  def inject(result: ExportResult, rootDir: File) = {
    def serviceFieldsFor(s: String) = {
      val newContent = {
        val withPackages = result.models.filter(_.pkg.nonEmpty).filterNot(_.provided).map(_.pkg.head).distinct.sorted.map { p =>
          s"""    val ${p}Services: ${result.config.corePrefix}services.$p.${ExportHelper.toClassName(p)}ServiceRegistry,"""
        }.sorted.mkString("\n")

        val withoutPackages = result.models.filter(_.pkg.isEmpty).filter(!_.provided).map { m =>
          s"""  val ${m.propertyName}Service: ${result.config.pkgPrefix}services.${m.className}Service,"""
        }.sorted.mkString("\n")

        val ws = if (withPackages.nonEmpty && withoutPackages.nonEmpty) { "\n\n" } else { "" }
        (withPackages + ws + withoutPackages).dropRight(1)
      }
      InjectHelper.replaceBetween(original = s, start = "    /* Start model service files */", end = "/* End model service files */", newContent = newContent)
    }

    val srSourceFile = rootDir / "app" / (result.config.pkgPrefix :+ "services").mkString("/") / "ServiceRegistry.scala"
    val newContent = serviceFieldsFor(srSourceFile.contentAsString)
    srSourceFile.overwrite(newContent)

    "ServiceRegistry.scala" -> newContent
  }
}
