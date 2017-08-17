package services.scalaexport.file

import models.scalaexport.RoutesFile
import services.scalaexport.config.ExportModel

object RoutesFiles {
  val listArgs = "q: Option[String] ?= None, orderBy: Option[String] ?= None, orderAsc: Boolean ?= true, " +
    "limit: Option[Int] ?= None, offset: Option[Int] ?= None"

  def routesContentFor(m: ExportModel) = {
    if (m.provided) {
      m -> Nil
    } else {
      val comment = s"# ${m.title} Routes"

      val listWs = (0 until (56 - m.propertyName.length)).map(_ => " ").mkString

      val list = s"GET         /${m.propertyName} $listWs ${m.controllerClass}.list($listArgs)"

      val createForm = s"GET         /${m.propertyName}/form ${listWs.drop(5)} ${m.controllerClass}.createForm"
      val createAct = s"POST        /${m.propertyName} $listWs ${m.controllerClass}.create"

      val detail = m.pkFields match {
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

      m -> (comment +: list +: createForm +: createAct +: detail :+ "")
    }
  }

  def files(models: Seq[ExportModel]) = {
    val packageModels = models.filter(_.pkg.nonEmpty)

    val routesContent = packageModels.map(routesContentFor)

    val packages = routesContent.groupBy(_._1.pkg.head).mapValues(_.flatMap(_._2)).toSeq.filter(_._2.nonEmpty).sortBy(_._1)

    packages.map { p =>
      val f = RoutesFile(p._1)
      p._2.foreach(l => f.add(l))
      f
    }
  }
}
