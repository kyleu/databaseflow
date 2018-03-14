package services.scalaexport.db.inject

import better.files.File
import models.scalaexport.db.ExportResult

object InjectRoutes {
  def inject(result: ExportResult, rootDir: File) = {
    val packages = result.models.filter(_.pkg.nonEmpty).filter(!_.provided).map(_.pkg.head).distinct.sorted

    def routeFor(pkg: String) = {
      val detailUrl = s"/admin/$pkg"
      val detailWs = (0 until (39 - detailUrl.length)).map(_ => " ").mkString
      s"->          $detailUrl $detailWs $pkg.Routes"
    }

    def routesFor(s: String) = {
      val newContent = packages.map(routeFor).mkString("\n")
      InjectHelper.replaceBetween(
        original = s,
        start = "# Start model route files",
        end = "# End model route files",
        newContent = newContent
      )
    }

    val routesFile = rootDir / "conf" / "routes"
    val newContent = routesFor(routesFile.contentAsString)
    routesFile.overwrite(newContent)

    "routes" -> newContent
  }
}
