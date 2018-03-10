package services.scalaexport.file.db

import models.scalaexport.ScalaFile
import services.scalaexport.ExportHelper
import services.scalaexport.config.ExportModel

object ServiceRegistryFiles {
  def files(models: Seq[ExportModel]) = {
    val packageModels = models.filter(_.pkg.nonEmpty).filterNot(_.provided)
    val packages = packageModels.groupBy(_.pkg.head).toSeq.filter(_._2.nonEmpty).sortBy(_._1)

    val routesContent = packages.map(m => m._1 -> m._2.map { m =>
      s"""    val ${m.propertyName}Service: ${m.serviceClass}"""
    }.sorted)

    routesContent.map { p =>
      val name = ExportHelper.toClassName(p._1) + "ServiceRegistry"
      val file = ScalaFile(Seq("services", p._1), name)
      file.add("@javax.inject.Singleton")
      file.add(s"class $name @javax.inject.Inject() (")
      file.add(p._2.mkString(",\n"))
      file.add(")")

      file
    }
  }
}
