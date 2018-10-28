package com.databaseflow.services.scalaexport.db.file

import com.databaseflow.models.scalaexport.db.ExportModel
import com.databaseflow.models.scalaexport.db.config.ExportConfiguration
import com.databaseflow.models.scalaexport.file.ScalaFile

object DoobieTestsFile {
  def export(config: ExportConfiguration, model: ExportModel) = {
    val file = ScalaFile(pkg = model.doobiePackage, key = model.className + "DoobieTests", root = Some("."), test = true)

    file.addImport("org.scalatest", "_")
    file.addImport(model.modelPackage.mkString("."), model.className)
    file.addImport(config.providedPrefix + "services.database.doobie.DoobieQueryService.Imports", "_")

    model.fields.foreach(_.enumOpt.foreach { e =>
      file.addImport(s"${e.doobiePackage.mkString(".")}.${e.className}Doobie", s"${e.propertyName}Meta")
    })

    file.add(s"class ${model.className}DoobieTests extends FlatSpec with Matchers {", 1)
    file.add(s"import ${config.providedPrefix}models.doobie.DoobieTestHelper.yolo._")

    file.add()
    file.add(s""""Doobie queries for [${model.className}]" should "typecheck" in {""", 1)
    file.add(s"${model.className}Doobie.countFragment.query[Long].check.unsafeRunSync")
    file.add(s"${model.className}Doobie.selectFragment.query[${model.className}].check.unsafeRunSync")
    val search = s"""(${model.className}Doobie.selectFragment ++ whereAnd(${model.className}Doobie.searchFragment("...")))"""
    file.add(s"$search.query[${model.className}].check.unsafeRunSync")
    file.add("}", -1)

    file.add("}", -1)

    file
  }
}
