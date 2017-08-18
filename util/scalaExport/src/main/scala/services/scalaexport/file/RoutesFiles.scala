package services.scalaexport.file

import models.scalaexport.RoutesFile
import services.scalaexport.config.ExportModel

object RoutesFiles {
  val listArgs = "q: Option[String] ?= None, orderBy: Option[String] ?= None, orderAsc: Boolean ?= true, " +
    "limit: Option[Int] ?= None, offset: Option[Int] ?= None"

  def routesContentFor(m: ExportModel, solo: Boolean = false) = {
    if (m.provided) {
      Nil
    } else if (solo) {
      val comment = s"# ${m.title} Routes"
      val listWs = (0 until 56).map(_ => " ").mkString
      val list = s"GET         / $listWs ${m.controllerClass}.list($listArgs)"
      val createForm = s"GET         /form ${listWs.drop(4)} ${m.controllerClass}.createForm"
      val createAct = s"POST        / $listWs ${m.controllerClass}.create"
      val detail = m.pkFields match {
        case Nil => Nil
        case pkFields =>
          val args = pkFields.map(x => s"${x.propertyName}: ${x.t.asScalaFull}").mkString(", ")
          val urlArgs = pkFields.map(x => ":" + x.propertyName).mkString("/")

          val detailWs = (0 until (56 - urlArgs.length)).map(_ => " ").mkString
          Seq(
            s"GET         /$urlArgs $detailWs ${m.controllerClass}.view($args)",
            s"GET         /$urlArgs/form ${detailWs.drop(5)} ${m.controllerClass}.editForm($args)",
            s"POST        /$urlArgs $detailWs ${m.controllerClass}.edit($args)",
            s"GET         /$urlArgs/remove ${detailWs.drop(7)} ${m.controllerClass}.remove($args)"
          )
      }
      comment +: list +: createForm +: createAct +: detail :+ ""
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
      comment +: list +: createForm +: createAct +: detail :+ ""
    }
  }

  def files(models: Seq[ExportModel]) = {
    val packageModels = models.filter(_.pkg.nonEmpty).filterNot(_.provided)
    val packages = packageModels.groupBy(_.pkg.head).toSeq.filter(_._2.nonEmpty).sortBy(_._1)

    val routesContent = packages.map {
      case p if p._2.size == 1 => p._1 -> routesContentFor(p._2.head, solo = true)
      case p => p._1 -> p._2.flatMap(m => routesContentFor(m))
    }

    routesContent.map { p =>
      val f = RoutesFile(p._1)
      p._2.foreach(l => f.add(l))
      f
    }
  }
}
