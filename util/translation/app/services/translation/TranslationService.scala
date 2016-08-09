package services.translation

import java.io.{File, PrintWriter}

import play.api.libs.concurrent.Execution.Implicits.defaultContext
import services.translation.api.{BingApi, YandexApi}

import scala.concurrent.Future
import scala.io.Source

@javax.inject.Singleton()
class TranslationService @javax.inject.Inject() (yandex: YandexApi, bing: BingApi) {
  val langs = Seq("cn", "de", "es", "fr")

  def parse(f: File) = Source.fromFile(f).getLines.flatMap { line =>
    val split = line.trim.split('=')
    if (split.length < 2) {
      None
    } else {
      Some(split.head.trim -> split.tail.mkString("=").trim)
    }
  }.toList

  def translate(apiName: String, main: Seq[(String, String)], lang: String, current: Map[String, String]) = {
    val api = getApi(apiName)
    val futures = main.map { en =>
      current.get(en._1) match {
        case Some(value) => Future.successful(Some(en._1 -> value))
        case None => api.translate(lang, en._1, en._2)
      }
    }
    val contents = Future.sequence(futures).map(_.flatten)
    contents
  }

  def translateAll(api: String, root: java.io.File) = {
    val mainFile = new java.io.File(root, "messages")
    val mainTranslations = parse(mainFile)

    val files = getLanguages(root).map { lang =>
      val f = new java.io.File(root, "messages." + lang)
      val current = parse(f).toMap
      val ts = translate(api, mainTranslations, lang, current)
      val contents = ts.map(seq => seq.map(x => s"${x._1} = ${x._2}").mkString("\n"))
      contents.flatMap { c =>
        new PrintWriter(f) {
          write(c)
          close()
        }
        ts.map(x => lang -> x)
      }
    }
    mainTranslations -> Future.sequence(files)
  }

  private[this] def getLanguages(root: java.io.File) = {
    val files = root.list.filter(_.contains("messages")).toSeq
    files.flatMap {
      case "messages" => None
      case f => Some(f.split('.').last)
    }
  }

  private[this] def getApi(name: String) = name match {
    case "yandex" => yandex
    case "bing" => bing
    case x => throw new IllegalArgumentException(s"Invalid api [$x].")
  }
}
