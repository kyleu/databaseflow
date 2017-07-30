package services.scalaexport.inject

import better.files.File
import models.scalaexport.ExportResult
import services.scalaexport.ExportHelper

object InjectRoutes {
  def inject(result: ExportResult, rootDir: File) = {
    def routesFor(s: String) = {
      val newContent = result.models.map { m =>
        val controller = ("controllers" +: "admin" +: m._1 :+ (ExportHelper.toClassName(m._2) + "Controller")).mkString(".")

        val comment = s"# ${ExportHelper.toClassName(m._2)} Routes"

        val listUrl = ExportHelper.toIdentifier(m._2)
        val listWhitespace = (0 until (22 - listUrl.length)).map(_ => " ").mkString
        val list = s"GET         /$listUrl $listWhitespace $controller.list(limit: Option[Int] ?= None, offset: Option[Int] ?= None)"

        val detailUrl = ExportHelper.toIdentifier(m._2) + "/:id"
        val detailWhitespace = (0 until (22 - detailUrl.length)).map(_ => " ").mkString
        val detail = s"GET         /$detailUrl $detailWhitespace $controller.view(id: Int)"

        Seq(comment, list, detail).mkString("\n") + "\n\n"
      }.mkString
      s.replaceAllLiterally(".saveSettings\n\n", s".saveSettings\n\n$newContent")
    }

    val routesFile = rootDir / "conf" / "admin.routes"
    val newContent = routesFor(routesFile.contentAsString)
    routesFile.overwrite(newContent)

    "admin.routes" -> newContent
  }
}
