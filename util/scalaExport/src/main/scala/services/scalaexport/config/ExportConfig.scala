package services.scalaexport.config

object ExportConfig {
  case class Result(
    key: String,
    projectName: String,
    classNames: Map[String, String],
    propertyNames: Map[String, String],
    packages: Map[String, String],
    searchColumns: Map[String, Seq[String]]
  )

  def emptyResult(k: String) = {
    val em = Map.empty[String, String]
    val emSeq = Map.empty[String, Seq[String]]
    Result(k, k, em, em, em, emSeq)
  }
}
