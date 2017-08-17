package services.scalaexport

import better.files._
import models.scalaexport.ExportResult
import services.scalaexport.inject._

object ExportInject {
  def inject(result: ExportResult, rootDir: File) = {
    val s = InjectSchema.inject(result, rootDir)
    val i = InjectIcons.inject(result, rootDir)
    val ro = InjectRoutes.inject(result, rootDir)
    val sro = InjectSystemRoutes.inject(result, rootDir)
    val xm = InjectExplore.injectMenu(result, rootDir)
    val xh = InjectExplore.injectHtml(result, rootDir)
    val sr = InjectSearch.inject(result, rootDir)

    result.log("Injection complete.")
    Seq(s, i, ro, sro, xm, xh, sr)
  }
}
