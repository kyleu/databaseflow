package services.scalaexport.file

import models.scalaexport.ScalaFile
import services.scalaexport.ExportTable

object ControllerFile {
  def export(et: ExportTable) = {
    val file = ScalaFile("controllers" +: "admin" +: et.pkg, et.className + "Controller")

    file.addImport("util", "Application")
    file.addImport("util.FutureUtils", "defaultContext")
    file.addImport("scala.concurrent", "Future")
    file.addImport("controllers", "BaseController")
    if (et.pkg.isEmpty) {
      file.addImport(s"services", s"${et.className}Service")
    } else {
      file.addImport(s"services.${et.pkg.mkString(".")}", s"${et.className}Service")
    }

    file.add("@javax.inject.Singleton")
    file.add(s"class ${et.className}Controller @javax.inject.Inject() (override val app: Application) extends BaseController {", 1)

    file.add(s"""def list(limit: Option[Int], offset: Option[Int] = None) = withSession("${et.propertyName}.list") { implicit request =>""", 1)
    file.add(s"""${et.className}Service.getAll(limit = limit, offset = offset).map { models =>""", 1)
    file.add("""Ok("Whoohoo!")""")
    file.add("}", -1)
    file.add("}", -1)

    file.add(s"""def view(id: Int) = withSession("${et.propertyName}.view") { implicit request =>""", 1)
    file.add(s"""${et.className}Service.getById(id).map { models =>""", 1)
    file.add("""Ok("Whoohoo!")""")
    file.add("}", -1)
    file.add("}", -1)

    /*
    et.t.columns.foreach { col =>
      col.columnType.requiredImport.foreach(p => file.addImport(p, col.columnType.asScala))

      val propName = ExportHelper.toIdentifier(col.name)
      val colScala = col.columnType match {
        case ColumnType.ArrayType => ColumnType.ArrayType.forSqlType(col.sqlTypeName)
        case x => x.asScala
      }
      val propType = if (col.notNull) { colScala } else { "Option[" + colScala + "]" }
      val propDefault = if (col.columnType == ColumnType.StringType) {
        col.defaultValue.map(v => " = \"" + v + "\"").getOrElse("")
      } else {
        ""
      }
      val propDecl = s"$propName: $propType$propDefault"
      val comma = if (et.t.columns.lastOption.contains(col)) { "" } else { "," }
      col.description.foreach(d => file.add("/** " + d + " */"))
      file.add(propDecl + comma)
    }
    */

    file.add("}", -1)
    file
  }

}
