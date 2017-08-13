package services.scalaexport.inject

import better.files.File
import models.scalaexport.ExportResult
import services.scalaexport.ExportHelper

object InjectRoutes {
  def inject(result: ExportResult, rootDir: File) = {
    def routesFor(s: String) = {
      val newContent = result.models.map { m =>
        if (m.provided) {
          ""
        } else {
          val controller = ("controllers" +: "admin" +: m.pkg :+ (m.className + "Controller")).mkString(".")

          val comment = s"# ${m.title} Routes"

          val listWs = (0 until (56 - m.propertyName.length)).map(_ => " ").mkString
          val listArgs = "q: Option[String] ?= None, orderBy: Option[String] ?= None, orderAsc: Boolean ?= true, " +
            "limit: Option[Int] ?= None, offset: Option[Int] ?= None"
          val list = s"GET         /${m.propertyName} $listWs $controller.list($listArgs)"

          val createForm = s"GET         /${m.propertyName}/form ${listWs.drop(5)} $controller.createForm"
          val createAct = s"POST        /${m.propertyName} $listWs $controller.create"

          val et = result.getModel(m.propertyName)
          val detail = et.pkColumns match {
            case Nil => Nil
            case pkCols =>
              val args = pkCols.map(x => s"${ExportHelper.toIdentifier(x.name)}: ${x.columnType.asScalaFull}").mkString(", ")
              val urlArgs = pkCols.map(x => ":" + ExportHelper.toIdentifier(x.name)).mkString("/")

              val detailUrl = m.propertyName + "/" + urlArgs
              val detailWs = (0 until (56 - detailUrl.length)).map(_ => " ").mkString
              Seq(
                s"GET         /$detailUrl $detailWs $controller.view($args)",
                s"GET         /$detailUrl/form ${detailWs.drop(5)} $controller.editForm($args)",
                s"POST        /$detailUrl $detailWs $controller.edit($args)"
              )
          }

          (comment +: list +: createForm +: createAct +: detail).mkString("\n") + "\n\n"
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
