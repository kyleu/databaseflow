package services.scalaexport.inject

import better.files.File
import models.scalaexport.ExportResult

object InjectRoutes {
  def inject(result: ExportResult, rootDir: File) = {
    def routesFor(s: String) = {
      val newContent = result.models.map { m =>
        if (m.provided) {
          ""
        } else {
          val comment = s"# ${m.title} Routes"

          val listWs = (0 until (56 - m.propertyName.length)).map(_ => " ").mkString
          val listArgs = "q: Option[String] ?= None, orderBy: Option[String] ?= None, orderAsc: Boolean ?= true, " +
            "limit: Option[Int] ?= None, offset: Option[Int] ?= None"
          val list = s"GET         /${m.propertyName} $listWs ${m.controllerClass}.list($listArgs)"

          val createForm = s"GET         /${m.propertyName}/form ${listWs.drop(5)} ${m.controllerClass}.createForm"
          val createAct = s"POST        /${m.propertyName} $listWs ${m.controllerClass}.create"

          val model = result.getModel(m.propertyName)
          val detail = model.pkFields match {
            case Nil => Nil
            case pkFields =>
              val args = pkFields.map(x => s"${x.propertyName}: ${x.t.asScalaFull}").mkString(", ")
              val urlArgs = pkFields.map(x => ":" + x.propertyName).mkString("/")

              val detailUrl = m.propertyName + "/" + urlArgs
              val detailWs = (0 until (56 - detailUrl.length)).map(_ => " ").mkString
              Seq(
                s"GET         /$detailUrl $detailWs ${m.controllerClass}.view($args)",
                s"GET         /$detailUrl/form ${detailWs.drop(5)} ${m.controllerClass}.editForm($args)",
                s"POST        /$detailUrl $detailWs ${m.controllerClass}.edit($args)",
                s"GET         /$detailUrl/remove ${detailWs.drop(7)} ${m.controllerClass}.remove($args)"
              )
          }

          (comment +: list +: createForm +: createAct +: detail).mkString("\n") + "\n\n"
        }
      }.mkString.stripSuffix("\n\n")

      InjectHelper.replaceBetween(original = s, start = "# Start model routes", end = "# End model routes", newContent = newContent)
    }

    val routesFile = rootDir / "conf" / "system.routes"
    val newContent = routesFor(routesFile.contentAsString)
    routesFile.overwrite(newContent)

    "admin.routes" -> newContent
  }
}
