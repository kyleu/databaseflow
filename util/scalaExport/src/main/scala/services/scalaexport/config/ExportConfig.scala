package services.scalaexport.config

object ExportConfig {
  case class Result(
      key: String,
      projectName: String,
      projectLocation: Option[String],
      provided: Map[String, String],
      classNames: Map[String, String],
      extendModels: Map[String, String],
      propertyNames: Map[String, String],
      packages: Map[String, String],
      searchColumns: Map[String, Seq[String]]
  ) {
    def withDefaults = copy(
      provided = Map() ++ provided,
      classNames = Map("users" -> "User") ++ classNames,
      extendModels = Map("user" -> "com.mohiva.play.silhouette.api.Identity") ++ extendModels,
      propertyNames = Map() ++ propertyNames,
      packages = Map("users" -> "user", "passwordInfo" -> "user", "ddl" -> "ddl") ++ packages,
      searchColumns = Map() ++ searchColumns
    )
  }
}
