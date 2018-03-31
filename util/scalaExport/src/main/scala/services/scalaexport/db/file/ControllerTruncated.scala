package services.scalaexport.db.file

import models.scalaexport.db.ExportModel
import models.scalaexport.db.config.ExportConfiguration
import models.scalaexport.file.ScalaFile

object ControllerTruncated {
  def export(config: ExportConfiguration, model: ExportModel) = {
    val file = ScalaFile(model.controllerPackage, model.className + "Controller")
    val viewHtmlPackage = model.viewHtmlPackage.mkString(".")
    file.addImport("models", "Application")
    file.addImport("_root_.util.FutureUtils", "defaultContext")
    file.addImport("controllers.admin", "ServiceController")
    file.addImport("services.audit", "AuditRecordService")
    file.addImport("_root_.util.JsonSerializers", "_")
    file.addImport("_root_.util.ReftreeUtils", "_")
    file.addImport("play.api.http", "MimeTypes")
    file.addImport(model.servicePackage.mkString("."), model.className + "Service")

    val extraImports = model.tableName match {
      case "system_users" => Seq(
        "com.mohiva.play.silhouette.api.repositories" -> "AuthInfoRepository",
        "com.mohiva.play.silhouette.api.util" -> "PasswordHasher"
      )
      case _ => Nil
    }
    extraImports.foreach(i => file.addImport(i._1, i._2))

    file.add("@javax.inject.Singleton")
    file.add(s"class ${model.className}Controller @javax.inject.Inject() (", 2)
    ControllerReferences.refServiceArgs(config, model, file) match {
      case ref if ref.trim.isEmpty => file.add(s"override val app: Application, svc: ${model.className}Service, auditRecordSvc: AuditRecordService")
      case ref =>
        val extraDeps = model.tableName match {
          case "system_users" => " val authInfoRepository: AuthInfoRepository, val hasher: PasswordHasher,"
          case _ => ""
        }
        file.add(s"override val app: Application, svc: ${model.className}Service, auditRecordSvc: AuditRecordService,$extraDeps")
        file.add(ref)
    }
    file.add(s") extends ServiceController(svc) with UserEditHelper with UserSearchHelper {", -2)
    file.indent()
    ControllerHelper.writeView(file, model, viewHtmlPackage)
    ControllerReferences.write(config, model, file)
    file.add("}", -1)
    file
  }
}
