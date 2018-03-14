package services.scalaexport.db.inject

import better.files.File
import models.scalaexport.db.ExportResult

object InjectBindables {
  def inject(result: ExportResult, rootDir: File) = {
    def fieldsFor(s: String) = {
      val startString = "  /* Begin model bindables */"
      val newContent = result.config.enums.sortBy(_.name).map { enum =>
        val sb = new StringBuilder()

        sb.append(s"  import ${enum.modelPackage.mkString(".")}.${enum.className}\n")

        sb.append(s"  private[this] def ${enum.propertyName}Extractor(v: Either[String, String]) = v match {\n")
        sb.append(s"    case Right(s) => Right(${enum.className}.withValue(s))\n")
        sb.append(s"    case Left(x) => throw new IllegalStateException(x)\n")
        sb.append("  }\n")

        sb.append(s"  implicit def ${enum.propertyName}PathBindable(implicit stringBinder: PathBindable[String]): PathBindable[${enum.className}] = new PathBindable[${enum.className}] {\n")
        sb.append(s"    override def bind(key: String, value: String) = ${enum.propertyName}Extractor(stringBinder.bind(key, value))\n")
        sb.append(s"    override def unbind(key: String, x: ${enum.className}) = x.value\n")
        sb.append("  }\n")

        sb.append(s"  implicit def ${enum.propertyName}QueryStringBindable(implicit stringBinder: QueryStringBindable[String]): QueryStringBindable[${enum.className}] = new QueryStringBindable[${enum.className}] {\n")
        sb.append(s"    override def bind(key: String, params: Map[String, Seq[String]]) = stringBinder.bind(key, params).map(${enum.propertyName}Extractor)\n")
        sb.append(s"    override def unbind(key: String, x: ${enum.className}) = x.value\n")
        sb.append("  }\n")

        sb.toString
      }.mkString("\n")
      InjectHelper.replaceBetween(original = s, start = startString, end = "  /* End model bindables */", newContent = newContent)
    }

    val sourceFile = rootDir / "app" / "util" / "web" / "ModelBindables.scala"
    val newContent = fieldsFor(sourceFile.contentAsString)
    sourceFile.overwrite(newContent)

    "ModelBindables.scala" -> newContent
  }
}
