package com.databaseflow.services.scalaexport.db.file

import com.databaseflow.models.scalaexport.db.ExportModel
import com.databaseflow.models.scalaexport.file.ScalaFile
import com.databaseflow.services.scalaexport.ExportHelper

object ServiceRegistryFiles {
  def files(models: Seq[ExportModel], pkgPrefix: List[String]) = {
    val packageModels = models.filter(_.pkg.nonEmpty).filterNot(_.provided)
    val packages = packageModels.groupBy(_.pkg.head).toSeq.filter(_._2.nonEmpty).sortBy(_._1)

    val routesContent = packages.map(m => m._1 -> m._2.map { m =>
      s"""    val ${m.propertyName}Service: ${m.serviceClass}"""
    }.sorted)

    routesContent.map { p =>
      val name = ExportHelper.toClassName(p._1) + "ServiceRegistry"
      val file = ScalaFile(pkgPrefix ++ Seq("services", p._1), name)
      file.add("@javax.inject.Singleton")
      file.add(s"class $name @javax.inject.Inject() (")
      file.add(p._2.mkString(",\n"))
      file.add(")")

      file
    }
  }
}
