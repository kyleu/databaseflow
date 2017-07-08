package models.scalaexport

case class ExportResult(id: String, models: Seq[(Seq[String], String)], files: Seq[ScalaFile]) {
  private[this] val startTime = System.currentTimeMillis
  private[this] val logs = collection.mutable.ArrayBuffer.empty[(Int, String)]

  def log(msg: String) = logs += ((System.currentTimeMillis - startTime).toInt -> msg)
  val getLogs: Seq[(Int, String)] = logs
}
