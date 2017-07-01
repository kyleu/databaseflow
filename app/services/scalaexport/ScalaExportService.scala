package services.scalaexport

import models.schema.Schema

import scala.concurrent.Future

object ScalaExportService {
  def export(schema: Schema) = {
    val ret = Seq("index.scala" -> "Hello, world!")

    Future.successful(ret)
  }
}
