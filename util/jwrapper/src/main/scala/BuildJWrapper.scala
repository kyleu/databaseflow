import java.nio.file.{Files, Paths}

object BuildJWrapper {
  private[this] val rootDir = "/Users/kyle/Projects/Personal/databaseflow"

  def main(args: Array[String]): Unit = {
    val xml = XmlHelper.xml(rootDir).toString
    Files.write(Paths.get(rootDir + "/build/jwrapper/jwrapper.xml"), xml.getBytes)
  }
}
