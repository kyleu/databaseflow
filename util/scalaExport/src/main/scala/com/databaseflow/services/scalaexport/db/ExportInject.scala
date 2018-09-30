package com.databaseflow.services.scalaexport.db

import better.files.File
import com.databaseflow.models.scalaexport.db.ExportResult
import com.databaseflow.services.scalaexport.db.inject._

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

    val openapi = if (result.config.exportOpenApi) {
      Seq(InjectOpenApiSchema.inject(result, rootDir), InjectOpenApiPaths.inject(result, rootDir))
    } else {
      Nil
    }

    result.log("Injection complete.")
    Seq(s, i, svc, al, ar, ro, sro, xm, xh, sr, b) ++ thrift ++ openapi
  }
}
