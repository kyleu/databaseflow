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

          val listWs = (0 until (56 - prop.length)).map(_ => " ").mkString
          val list = s"GET         /$prop $listWs $controller.list(q: Option[String] ?= None, orderBy: Option[String] ?= None, orderAsc: Boolean ?= true, limit: Option[Int] ?= None, offset: Option[Int] ?= None)"

          val formNew = s"GET         /$prop/new ${listWs.drop(4)} $controller.formNew"

          val et = result.getExportTable(prop)
          val detail = et.pkColumns match {
            case Nil => Nil
            case pkCols =>
              val args = pkCols.map(x => s"${ExportHelper.toIdentifier(x.name)}: ${x.columnType.asScalaFull}").mkString(", ")
              val urlArgs = pkCols.map(x => ":" + ExportHelper.toIdentifier(x.name)).mkString("/")

              val detailUrl = prop + "/" + urlArgs
              val detailWs = (0 until (56 - detailUrl.length)).map(_ => " ").mkString
              Seq(
                s"GET         /$detailUrl $detailWs $controller.view($args)",
                s"GET         /$detailUrl/edit ${detailWs.drop(5)} $controller.formEdit($args)"
              )
          }

          (comment +: list +: formNew +: detail).mkString("\n") + "\n\n"
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
