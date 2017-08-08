package services.scalaexport.inject

import better.files.File
import models.scalaexport.ExportResult
import services.scalaexport.ExportHelper

object InjectRoutes {
  def inject(result: ExportResult, rootDir: File) = {
    def routesFor(s: String) = {
      val newContent = result.models.map { m =>
        val prop = ExportHelper.toIdentifier(m._2)

        if (result.config.provided.contains(prop)) {
          ""
        } else {
          val controller = ("controllers" +: "admin" +: m._1 :+ (ExportHelper.toClassName(m._2) + "Controller")).mkString(".")

          val comment = s"# ${ExportHelper.toClassName(m._2)} Routes"

          val listWs = (0 until (40 - prop.length)).map(_ => " ").mkString
          val list = s"GET         /$prop $listWs $controller.list(q: Option[String] ?= None, limit: Option[Int] ?= None, offset: Option[Int] ?= None)"

          val et = result.getExportTable(prop)
          val detail = et.pkColumns match {
            case Nil => Nil
            case pkCols =>
              val args = pkCols.map(x => s"${ExportHelper.toIdentifier(x.name)}: ${x.columnType.asScalaFull}").mkString(", ")
              val urlArgs = pkCols.map(x => ":" + ExportHelper.toIdentifier(x.name)).mkString("/")

              val detailUrl = prop + "/" + urlArgs
              val detailWhitespace = (0 until (40 - detailUrl.length)).map(_ => " ").mkString
              Seq(s"GET         /$detailUrl $detailWhitespace $controller.view($args)")
          }

          (Seq(comment, list) ++ detail).mkString("\n") + "\n\n"
        }
      }.mkString.stripSuffix("\n\n")

      InjectHelper.replaceBetween(original = s, start = "# Start model routes", end = "# End model routes", newContent = newContent)
    }

    val routesFile = rootDir / "conf" / "admin.routes"
    val newContent = routesFor(routesFile.contentAsString)
    routesFile.overwrite(newContent)

    "admin.routes" -> newContent
  }
}
