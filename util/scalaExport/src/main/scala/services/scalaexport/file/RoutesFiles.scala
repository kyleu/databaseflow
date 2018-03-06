package services.scalaexport.file

import models.scalaexport.RoutesFile
import services.scalaexport.config.{ExportConfiguration, ExportModel}

object RoutesFiles {
  val listArgs = "q: Option[String] ?= None, orderBy: Option[String] ?= None, orderAsc: Boolean ?= true, " +
    "limit: Option[Int] ?= None, offset: Option[Int] ?= None"
  val autocompleteArgs = "q: Option[String] ?= None, orderBy: Option[String] ?= None, orderAsc: Boolean ?= true, limit: Option[Int] ?= None"
  val relationArgs = "orderBy: Option[String] ?= None, orderAsc: Boolean ?= true, limit: Option[Int] ?= None, offset: Option[Int] ?= None"

  def routesContentFor(config: ExportConfiguration, model: ExportModel, solo: Boolean = false) = {
    if (model.provided) {
      Nil
    } else {
      val prefix = if (solo) { "" } else { s"/${model.propertyName}" }
      val root = if (solo) { "/" } else { s"/${model.propertyName}" }

      val comment = s"# ${model.title} Routes"
      val listWs = (0 until (56 - root.length)).map(_ => " ").mkString
      val list = s"GET         $root $listWs ${model.controllerClass}.list($listArgs)"
      val autocomplete = s"GET         $prefix/autocomplete ${listWs.drop(13)} ${model.controllerClass}.autocomplete($autocompleteArgs)"
      val createForm = s"GET         $prefix/form ${listWs.drop(5)} ${model.controllerClass}.createForm"
      val createAct = s"POST        $root $listWs ${model.controllerClass}.create"
      val fks = model.foreignKeys.flatMap { fk =>
        fk.references match {
          case h :: Nil =>
            val col = model.fields.find(_.columnName == h.source).getOrElse(throw new IllegalStateException(s"Missing column [${h.source}]."))
            val urlArgs = s"by${col.className}/:${col.propertyName}"
            val detailUrl = prefix + "/" + urlArgs
            val detailWs = (0 until (56 - detailUrl.length)).map(_ => " ").mkString
            Some(s"GET         $detailUrl $detailWs ${model.controllerClass}.by${col.className}(${col.propertyName}: ${col.scalaTypeFull}, $relationArgs)")
          case _ => None
        }
      }
      val detail = model.pkFields match {
        case Nil => Nil
        case pkFields =>
          val args = pkFields.map(x => s"${x.propertyName}: ${x.scalaTypeFull}").mkString(", ")
          val urlArgs = pkFields.map(x => ":" + x.propertyName).mkString("/")

          val detailUrl = prefix + "/" + urlArgs
          val detailWs = (0 until (56 - detailUrl.length)).map(_ => " ").mkString

          val view = s"GET         $detailUrl $detailWs ${model.controllerClass}.view($args)"
          val counts = if (model.validReferences(config).isEmpty) {
            Nil
          } else {
            Seq(s"GET         $detailUrl/counts ${detailWs.drop(7)} ${model.controllerClass}.relationCounts($args)")
          }
          val extras = Seq(
            s"GET         $detailUrl/form ${detailWs.drop(5)} ${model.controllerClass}.editForm($args)",
            s"POST        $detailUrl $detailWs ${model.controllerClass}.edit($args)",
            s"GET         $detailUrl/remove ${detailWs.drop(7)} ${model.controllerClass}.remove($args)"
          )
          view +: (counts ++ extras)
      }
      comment +: list +: autocomplete +: createForm +: createAct +: (fks ++ detail) :+ ""
    }
  }

  def files(config: ExportConfiguration, models: Seq[ExportModel]) = {
    val packageModels = models.filter(_.pkg.nonEmpty).filterNot(_.provided)
    val packages = packageModels.groupBy(_.pkg.head).toSeq.filter(_._2.nonEmpty).sortBy(_._1)

    val routesContent = packages.map {
      case p if p._2.lengthCompare(1) == 0 => p._1 -> routesContentFor(config, p._2.head, solo = true)
      case p => p._1 -> p._2.flatMap(m => routesContentFor(config, m))
    }

    routesContent.map { p =>
      val f = RoutesFile(p._1)
      p._2.foreach(l => f.add(l))
      f
    }
  }
}
