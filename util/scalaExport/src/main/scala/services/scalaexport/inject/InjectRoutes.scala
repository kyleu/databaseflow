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
        val listWhitespace = (0 until (32 - listUrl.length)).map(_ => " ").mkString
        val list = s"GET         /$listUrl $listWhitespace $controller.list(limit: Option[Int] ?= None, offset: Option[Int] ?= None)"

        val et = result.getExportTable(ExportHelper.toIdentifier(m._2))
        val detail = et.pkColumns match {
          case Nil => None
          case h :: Nil =>
            val hProp = ExportHelper.toIdentifier(h.name)
            val detailUrl = ExportHelper.toIdentifier(m._2) + s"/:$hProp"
            val detailWhitespace = (0 until (32 - detailUrl.length)).map(_ => " ").mkString
            Some(s"GET         /$detailUrl $detailWhitespace $controller.view($hProp: ${h.columnType.asScalaFull})")
          case _ => None // todo
        }

        (Seq(comment, list) ++ detail.toSeq).mkString("\n") + "\n\n"
      }.mkString.stripSuffix("\n\n")
      s.replaceAllLiterally("# Other models...", newContent)
    }

    val routesFile = rootDir / "conf" / "admin.routes"
    val newContent = routesFor(routesFile.contentAsString)
    routesFile.overwrite(newContent)

    "admin.routes" -> newContent
  }
}
