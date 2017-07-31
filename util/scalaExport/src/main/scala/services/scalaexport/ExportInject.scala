package services.scalaexport

import better.files._
import models.scalaexport.ExportResult
import services.scalaexport.inject.{InjectExplore, InjectIcons, InjectRoutes, InjectSchema}

object ExportInject {
  def inject(result: ExportResult) = {
    val rootDir = s"./tmp/${ExportHelper.toIdentifier(result.id)}".toFile

    val s = InjectSchema.inject(result, rootDir)
    val i = InjectIcons.inject(result, rootDir)
    val r = InjectRoutes.inject(result, rootDir)
    val xm = InjectExplore.injectMenu(result, rootDir)
    val xh = InjectExplore.injectHtml(result, rootDir)

    result.log("Injection complete.")
    Seq(s, i, r, xm, xh)
  }
}
