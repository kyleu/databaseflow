import java.nio.file.{Files, Paths}

object BuildJWrapper {
  private[this] val rootDir = "/Users/kyle/Projects/Personal/databaseflow"
  private[this] val inputDir = "target/universal/stage/"
  private[this] val outputDir = new java.io.File("build/jwrapper")

  def main(args: Array[String]): Unit = {
    run()
  }

  def run() = {
    val xml = XmlHelper.xml(rootDir).toString
    Files.write(Paths.get(rootDir + "/bin/jwrapper.xml"), xml.getBytes)

    //val args = Array("-Xmx2048m", "bin/jwrapper.xml")
    //jwrapper.launch.JWCompiler.main(args)
  }
}
