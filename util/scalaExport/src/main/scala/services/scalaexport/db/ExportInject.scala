package services.scalaexport.db

import better.files.File
import models.scalaexport.db.ExportResult
import services.scalaexport.db.inject._

object ExportInject {
  def inject(result: ExportResult, rootDir: File) = {
    val s = InjectSchema.inject(result, rootDir)
    val i = InjectIcons.inject(result, rootDir)
    val svc = InjectServiceRegistry.inject(result, rootDir)
    val al = InjectAuditLookup.inject(result, rootDir)
    val ar = InjectAuditRoutes.inject(result, rootDir)
    val ro = InjectRoutes.inject(result, rootDir)
    val sro = InjectSystemRoutes.inject(result, rootDir)
    val xm = InjectExplore.injectMenu(result, rootDir)
    val xh = InjectExplore.injectHtml(result, rootDir)
    val sr = InjectSearch.inject(result, rootDir)

    val b = InjectBindables.inject(result, rootDir)

    val thrift = InjectThrift.inject(result, rootDir)

    result.log("Injection complete.")
    Seq(s, i, svc, al, ar, ro, sro, xm, xh, sr, b) ++ thrift
  }
}
