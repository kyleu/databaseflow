package com.databaseflow.services.scalaexport.db.inject

import better.files.File
import com.databaseflow.models.scalaexport.db.ExportResult
import com.databaseflow.services.scalaexport.db.file.RoutesFiles

object InjectSystemRoutes {
  def inject(result: ExportResult, rootDir: File) = {
    val systemModels = result.models.filter(_.pkg.isEmpty)

    def routesFor(s: String) = {
      val newContent = systemModels.flatMap(m => RoutesFiles.routesContentFor(result.config, m)).mkString("\n")
      InjectHelper.replaceBetween(original = s, start = "# Start model routes", end = "# End model routes", newContent = newContent)
    }

    val routesFile = rootDir / "conf" / "system.routes"
    val newContent = routesFor(routesFile.contentAsString)
    routesFile.overwrite(newContent)

    "system.routes" -> newContent
  }
}
