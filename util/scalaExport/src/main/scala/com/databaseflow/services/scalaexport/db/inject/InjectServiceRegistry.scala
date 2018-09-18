package com.databaseflow.services.scalaexport.db.inject

import better.files.File
import com.databaseflow.models.scalaexport.db.ExportResult
import com.databaseflow.services.scalaexport.ExportHelper

object InjectServiceRegistry {
  def inject(result: ExportResult, rootDir: File) = {
    def serviceFieldsFor(s: String) = {
      val startString = "    /* Start model service files */"
      val endString = "    /* End model service files */"
      val endIndex = s.indexOf(endString)

      val newContent = {
        val withPackages = result.models.filter(_.pkg.nonEmpty).filterNot(_.provided).map(_.pkg.head).distinct.sorted.flatMap { p =>
          s.indexOf(s"val ${p}Services") match {
            case x if x > -1 && x > endIndex => None
            case _ => Some(s"""    val ${p}Services: ${result.config.corePrefix}services.$p.${ExportHelper.toClassName(p)}ServiceRegistry,""")
          }
        }.sorted.mkString("\n")

        val withoutPackages = result.models.filter(_.pkg.isEmpty).filter(!_.provided).flatMap { m =>
          s.indexOf(s"val ${m.propertyName}Service") match {
            case x if x > -1 && x > endIndex => None
            case _ => Some(s"""  val ${m.propertyName}Service: ${result.config.pkgPrefix}services.${m.className}Service,""")
          }
        }.sorted.mkString("\n")

        val ws = if (withPackages.nonEmpty && withoutPackages.nonEmpty) { "\n\n" } else { "" }
        withPackages + ws + withoutPackages
      }
      InjectHelper.replaceBetween(original = s, start = startString, end = endString, newContent = newContent)
    }

    val srSourceFile = rootDir / "app" / (result.config.pkgPrefix :+ "services").mkString("/") / "ServiceRegistry.scala"
    val newContent = serviceFieldsFor(srSourceFile.contentAsString)
    srSourceFile.overwrite(newContent)

    "ServiceRegistry.scala" -> newContent
  }
}
