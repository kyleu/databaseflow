package services.scalaexport.inject

import better.files.File
import models.scalaexport.ExportResult
import services.scalaexport.ExportHelper

object InjectServiceRegistry {
  def inject(result: ExportResult, rootDir: File) = {
    def serviceFieldsFor(s: String) = {
      val newContent = {
        val withPackages = result.models.filter(_.pkg.nonEmpty).filterNot(_.provided).map(_.pkg.head).distinct.sorted.map { p =>
          s"""  val ${p}Services: services.$p.${ExportHelper.toClassName(p)}ServiceRegistry,"""
        }.sorted.mkString("\n")

        val withoutPackages = result.models.filter(_.pkg.isEmpty).filter(!_.provided).map { m =>
          s"""  val ${m.propertyName}Service: services.${m.className}Service,"""
        }.sorted.mkString("\n")

        val ws = if (withPackages.nonEmpty && withoutPackages.nonEmpty) { "\n\n" } else { "" }
        withPackages + ws + withoutPackages
      }
      InjectHelper.replaceBetween(original = s, start = "  /* Start model service files */", end = "  /* End model service files */", newContent = newContent)
    }

    val srSourceFile = rootDir / "app" / "services" / "ServiceRegistry.scala"
    val newContent = serviceFieldsFor(srSourceFile.contentAsString)
    srSourceFile.overwrite(newContent)

    "ServiceRegistry.scala" -> newContent
  }
}
